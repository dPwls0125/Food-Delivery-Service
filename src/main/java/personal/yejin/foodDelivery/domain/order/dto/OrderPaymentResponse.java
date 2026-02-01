package personal.yejin.foodDelivery.domain.order.dto;

import personal.yejin.foodDelivery.domain.order.model.OrderStatus;

import java.time.LocalDateTime;

public record OrderPaymentResponse (
    long orderId,
    OrderStatus orderStatus,
    PaymentStatus paymentStatus,
    int originalPrice,
    int discountAmount,
    int paidAmount,
    OrderPaymentRequest.PaymentMethod paymentMethod,
    LocalDateTime paidAt)
    {
    public enum PaymentStatus {
        SUCCESS, FAILED
    }
}
