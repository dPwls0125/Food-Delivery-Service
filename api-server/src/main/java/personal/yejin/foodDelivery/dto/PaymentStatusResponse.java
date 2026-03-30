package personal.yejin.foodDelivery.dto;

import personal.yejin.model.PaymentStatus;

public record PaymentStatusResponse(
        Long orderId,
        PaymentStatus status,
        String correlationId,
        String failureReason,
        String updatedAt
) {
}
