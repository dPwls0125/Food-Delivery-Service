package personal.yejin.kafkaListner;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.PaymentResultEvent;
import personal.yejin.model.Payment;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;
import personal.yejin.repository.PaymentRepository;
import personal.yejin.service.CardPaymentAPI;
import personal.yejin.service.CashPaymentAPI;
import personal.yejin.service.PaymentAPI;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestListener {

    private static final String KAFKA_PAYMENT_RESULT_TOPIC = "payment-result";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    //    private final StatusServerClient statusServerClient; TODO
    private final Map<PaymentMethod, PaymentAPI> paymentAPIMap;


    public PaymentRequestListener(KafkaTemplate<String, Object> kafkaTemplate, PaymentRepository paymentRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentRepository = paymentRepository;
        this.paymentAPIMap = new HashMap<>();
        paymentAPIMap.put(PaymentMethod.CARD, new CardPaymentAPI());
        paymentAPIMap.put(PaymentMethod.CASH, new CashPaymentAPI());
    }

    @Transactional
    @KafkaListener(topics = "payment-request", groupId = "payment-process-group")
    public void consumePaymentRequest(PaymentRequestEvent request) {

        // 1. Payment 엔티티 PENDING 상태로 저장
        Payment payment = savePaymentInPendingStatus(request);

        // 2. 결제 처리 (예외 발생해도 FAILED 이벤트 발행되도록 처리)
        boolean paySuccess = false;
        String failureReason = null;

        PaymentMethod paymentMethod = request.paymentMethod();

        try {
            callPaymentApi(request, paymentMethod);
            if (!paySuccess) {
                failureReason = "잔액 부족";
            }
        } catch (Exception e) {
            log.error("결제 처리 중 예외 발생. correlationId={}", request.correlationId(), e);
            failureReason = "결제 처리 중 오류: " + e.getMessage();
        }

        // 3. Payment 상태 업데이트
        PaymentStatus status = paySuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        payment.setStatus(status);
        payment.setFailReason(failureReason);

        // 4. Status Server에 상태 업데이트 // TODO
//        statusServerClient.updateStatus(request.correlationId(), status);

        // 5. 결과 이벤트 발행 (성공/실패 모두)
        PaymentResultEvent event = new PaymentResultEvent(
                request.correlationId(),
                payment.getId(),
                LocalDateTime.now(),
                request.orderId(),
                request.userId(),
                request.finalPrice(),
                failureReason
        );

        kafkaTemplate.send(KAFKA_PAYMENT_RESULT_TOPIC, event);
        log.info("PYMENT_RESULT_TOPIC 밸행 : orderId={}, correlationId={}", request.orderId(), request.correlationId());
    }

    private void callPaymentApi(PaymentRequestEvent request, PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.CASH) {
            paymentAPIMap.get(PaymentMethod.CASH).pay(request.finalPrice(), paymentMethod);
        } else if (paymentMethod == PaymentMethod.CARD) {
            paymentAPIMap.get(PaymentMethod.CARD).pay(request.finalPrice(), paymentMethod);
        }
    }

    private Payment savePaymentInPendingStatus(PaymentRequestEvent request) {
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .userId(request.userId())
                .amount(request.finalPrice())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

}
