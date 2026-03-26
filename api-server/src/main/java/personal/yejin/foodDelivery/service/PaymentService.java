package personal.yejin.foodDelivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.foodDelivery.dto.PaymentResponse;
import personal.yejin.foodDelivery.exception.PaymentException;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResponse processPayment(long orderId, long userId, int finalPrice, PaymentMethod paymentMethod) {

        String correlationId = createCorrelationId();
        log.info("결제 요청 수신: orderId={}, correlationId={}", orderId, correlationId);

        try {
            publishPaymentRequestEvent(orderId, userId, correlationId, finalPrice, paymentMethod);
            log.info("결체 요청 이벤트 발행 완료 orderId={}, correlationId={}", orderId, correlationId);
            return new PaymentResponse(correlationId, PaymentStatus.PENDING);

        } catch (Exception e) {
            log.error("api-server to payment-request topic Kafka 전송 실패 또는 주문 검증 실패 : correlationId={}, orderId={}",
                    correlationId,
                    orderId, e);
            throw new PaymentException("결제 요청 실패", e);
        }
    }

    private String createCorrelationId() {
        return "payment-" + UUID.randomUUID();
    }

    private void publishPaymentRequestEvent(long orderId, long userId, String correlationId, int finalPrice,
            PaymentMethod paymentMethod) throws Exception {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("payment-request",
                new PaymentRequestEvent(
                        orderId,
                        userId,
                        correlationId,
                        finalPrice,
                        paymentMethod));

        future.get(5, TimeUnit.SECONDS);
    }

}
