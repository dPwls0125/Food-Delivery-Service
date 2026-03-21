package personal.yejin.foodDelivery.domain.order.dto;

public record OrderPaymentRequest (
    PaymentMethod paymentMethod,
    int finalPrice,
    long userId
    )
    {
        public enum PaymentMethod {
        CARD, CASH
    }
}
