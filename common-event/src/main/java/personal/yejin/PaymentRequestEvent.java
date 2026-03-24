package personal.yejin;

import personal.yejin.model.PaymentMethod;

public record PaymentRequestEvent(
        Long orderId,
        Long userId,
        String correlationId,
        Integer finalPrice,
        PaymentMethod paymentMethod
) {
}
