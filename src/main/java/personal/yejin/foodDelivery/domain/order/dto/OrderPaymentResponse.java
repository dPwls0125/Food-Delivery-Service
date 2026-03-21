package personal.yejin.foodDelivery.domain.order.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderPaymentResponse (
    long orderId,
    PaymentStatus paymentStatus,
    int paidAmount,
    OrderPaymentRequest.PaymentMethod paymentMethod,
    LocalDateTime paidAt)
    {
    public enum PaymentStatus {
        SUCCESS, FAILED
    }
}
