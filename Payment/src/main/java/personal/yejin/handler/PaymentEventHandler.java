package personal.yejin.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import personal.yejin.PaymentResultEvent;
import personal.yejin.client.StatusServerClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private static final String KAFKA_PAYMENT_RESULT_TOPIC = "payment-result";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StatusServerClient statusServerClient;

    /**
     * 트랜잭션 커밋 후 비동기로 실행된다.
     * - Kafka에 결제 결과 이벤트 발행
     * - Status Server에 결제 상태 업데이트
     */
    @Async("virtualThreadExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedInternalEvent event) {
        log.info("트랜잭션 커밋 후 이벤트 수신: orderId={}, correlationId={}, status={}",
                event.orderId(), event.correlationId(), event.paymentStatus());

        // 1. Kafka 결과 이벤트 발행
        PaymentResultEvent resultEvent = new PaymentResultEvent(
                event.correlationId(),
                event.paymentId(),
                event.orderId(),
                event.userId(),
                event.paymentStatus(),
                event.timestamp(),
                event.finalPrice(),
                event.failureReason()
        );


        try {
            kafkaTemplate.send(KAFKA_PAYMENT_RESULT_TOPIC, resultEvent);
            log.info("PAYMENT_RESULT_TOPIC 발행: orderId={}, correlationId={}",
                    event.orderId(), event.correlationId());
        } catch (Exception e) {
            log.error("Kafka PAYMENT_RESULT_TOPIC 발행 중 오류 발생 : orderId={}, correlationId={}",
                    event.orderId(), event.correlationId(), e);
        }

        // 2. Status Server에 상태 업데이트
        try {
            statusServerClient.updatePaymentStatus(
                    event.orderId(), event.correlationId(),
                    event.paymentStatus(), event.failureReason());
        } catch (Exception e) {
        }
    }
}
