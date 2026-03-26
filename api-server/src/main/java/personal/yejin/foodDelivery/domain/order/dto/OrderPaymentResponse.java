package personal.yejin.foodDelivery.domain.order.dto;

import lombok.Builder;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;

import java.time.LocalDateTime;

@Builder
public record OrderPaymentResponse (
    long orderId,
    PaymentStatus paymentStatus,
    int paidAmount,
    PaymentMethod paymentMethod,
    LocalDateTime paidAt)
    {
}
