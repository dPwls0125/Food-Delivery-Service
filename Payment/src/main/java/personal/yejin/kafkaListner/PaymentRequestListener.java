package personal.yejin.kafkaListner;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.PaymentResultEvent;
import personal.yejin.client.StatusServerClient;
import personal.yejin.config.ExecutorConfig;
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
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestListener {

    private static final String KAFKA_PAYMENT_RESULT_TOPIC = "payment-result";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private final StatusServerClient statusServerClient;
    private final ExecutorConfig executorConfig;
    private final Map<PaymentMethod, PaymentAPI> paymentAPIMap = new HashMap<>();

    @PostConstruct
    private void init() {
        paymentAPIMap.put(PaymentMethod.CARD, new CardPaymentAPI());
        paymentAPIMap.put(PaymentMethod.CASH, new CashPaymentAPI());
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

        // 3. Payment 상태 업데이트
        PaymentStatus paymentStatus = isPaymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAIL;
        payment.setStatus(paymentStatus);
        payment.setFailReason(failureReason);

        // 4. Status Server에 상태 업데이트
        sendPaymentStatusToStatusServerAsync(request.orderId(), request.correlationId(), failureReason, paymentStatus);

        // 5. 결과 이벤트 발행 (성공/실패 모두)
        PaymentResultEvent event = new PaymentResultEvent(
                request.correlationId(),
                payment.getId(),
                request.orderId(),
                request.userId(),
                paymentStatus,
                LocalDateTime.now(),
                request.finalPrice(),
                failureReason
        );

        kafkaTemplate.send(KAFKA_PAYMENT_RESULT_TOPIC, event);
        log.info("PYMENT_RESULT_TOPIC 밸행 : orderId={}, correlationId={}", request.orderId(), request.correlationId());
    }

    private void sendPaymentStatusToStatusServerAsync(long orderId, String correlationId, String failureReason, PaymentStatus status) {
        CompletableFuture.runAsync(() -> {
            statusServerClient.updatePaymentStatus(
                    orderId, correlationId, status, failureReason);
        }, executorConfig.virtualThreadExecutor()).exceptionally(ex -> {
            log.error("Status server 비동기 업데이트 중 오류 발생: orderId={}, correlationId={}", orderId, correlationId, ex);
            return null;
        });
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
