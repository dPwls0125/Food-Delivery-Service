package personal.yejin.kafkaListner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.handler.PaymentCompletedInternalEvent;
import personal.yejin.model.Payment;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;
import personal.yejin.repository.PaymentRepository;
import personal.yejin.service.CardPaymentAPI;
import personal.yejin.service.CashPaymentAPI;
import personal.yejin.service.PaymentAPI;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentRequestListener {
 
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<PaymentMethod, PaymentAPI> paymentAPIMap;

    public PaymentRequestListener(PaymentRepository paymentRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  List<PaymentAPI> paymentAPIs) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.paymentAPIMap = paymentAPIs.stream()
                .collect(Collectors.toMap(PaymentAPI::getSupportedMethod, Function.identity()));
    }

    @Transactional
    @KafkaListener(topics = "payment-request", groupId = "payment-process-group")
    public void consumePaymentRequest(PaymentRequestEvent request) {

        // 1. Payment 엔티티 PENDING 상태로 저장
        Payment payment = savePaymentInPendingStatus(request);

        // 2. 결제 처리 (예외 발생해도 FAILED 이벤트 발행되도록 처리)
        boolean isPaymentSuccess = false;
        String failureReason = null;

        PaymentMethod paymentMethod = request.paymentMethod();

        try {
            isPaymentSuccess = callPaymentApi(request, paymentMethod);
            if (!isPaymentSuccess) {
                failureReason = "잔액 부족"; // Todo : Mocking아니라 실제로 변경
            }
        } catch (Exception e) {
            log.error("결제 처리 중 예외 발생. correlationId={}", request.correlationId(), e);
            failureReason = "결제 처리 중 오류: " + e.getMessage();
        }

        // 3. Payment 상태 업데이트 (dirty checking으로 커밋 시 DB 반영)
        PaymentStatus paymentStatus = isPaymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAIL;
        payment.setStatus(paymentStatus);
        payment.setFailReason(failureReason);

        // 4. Spring 내부 이벤트 발행 → 트랜잭션 커밋 후 PaymentEventHandler가 수신
        eventPublisher.publishEvent(new PaymentCompletedInternalEvent(
                request.correlationId(),
                payment.getId(),
                request.orderId(),
                request.userId(),
                1L, // storeId
                paymentStatus,
                LocalDateTime.now(),
                request.finalPrice(), // amount
                failureReason
        ));

        log.info("결제 처리 완료, 내부 이벤트 발행: orderId={}, correlationId={}, status={}",
                request.orderId(), request.correlationId(), paymentStatus);
    }

    private boolean callPaymentApi(PaymentRequestEvent request, PaymentMethod paymentMethod) {
        PaymentAPI api = paymentAPIMap.get(paymentMethod);
        if (api != null) {
            return api.pay(request.finalPrice(), paymentMethod);
        }
        return false;
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
