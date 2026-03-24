package personal.yejin.foodDelivery.domain.payment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.dto.PaymentResponse;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public PaymentResponse processPayment(long orderId, OrderPaymentRequest request) {
        String correlationId = createCorrelationId();
        log.info("결제 요청 수신: orderId={}, correlationId={}", orderId, correlationId);

        publishPaymentRequestEvent(orderId, request.userId(), correlationId, request.finalPrice(), request.paymentMethod());
        log.info("결체 요청 이벤트 발행 완료 orderId={}, correlationId={}", orderId, correlationId);

        return new PaymentResponse(correlationId, PaymentStatus.PENDING);
    }

    private String createCorrelationId() {
        return "payment-" + UUID.randomUUID();
    }

    private void publishPaymentRequestEvent(long orderId, long userId, String correlationId, int finalPrice, PaymentMethod paymentMethod) {
        kafkaTemplate.send("payment-request", new PaymentRequestEvent(
                orderId,
                userId,
                correlationId,
                finalPrice,
                paymentMethod));
    }

}

