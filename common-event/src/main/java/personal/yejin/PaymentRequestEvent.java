package personal.yejin;

import personal.yejin.model.PaymentMethod;

public record PaymentRequestEvent(
        long orderId,
        long userId,
        String correlationId,
        int finalPrice,
        PaymentMethod paymentMethod
) {
}
