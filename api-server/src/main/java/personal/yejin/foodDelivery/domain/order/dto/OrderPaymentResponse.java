package personal.yejin.foodDelivery.domain.order.dto;

import lombok.Builder;
import personal.yejin.foodDelivery.domain.payment.model.PaymentStatus;

import java.time.LocalDateTime;

@Builder
public record OrderPaymentResponse (
    long orderId,
    PaymentStatus paymentStatus,
    int paidAmount,
    OrderPaymentRequest.PaymentMethod paymentMethod,
    LocalDateTime paidAt)
    {
}
