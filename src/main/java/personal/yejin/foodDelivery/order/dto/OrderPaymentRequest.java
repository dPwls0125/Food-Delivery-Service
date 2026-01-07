package personal.yejin.foodDelivery.order.dto;

public record OrderPaymentRequest (
    PaymentMethod paymentMethod,
    long couponId,
    boolean useBaeminClub)
    {
    public enum PaymentMethod {
        CARD, CASH
    }
}
