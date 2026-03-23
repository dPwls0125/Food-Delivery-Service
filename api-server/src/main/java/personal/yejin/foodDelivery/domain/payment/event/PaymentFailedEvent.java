package personal.yejin.foodDelivery.domain.payment.event;

public record PaymentFailedEvent(
        Long orderId,
        Long userId,
        String correlationId,
        String reason,
        Integer amount,
        String paymentMethod
) {}
