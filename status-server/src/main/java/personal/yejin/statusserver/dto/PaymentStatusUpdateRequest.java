package personal.yejin.statusserver.dto;

import personal.yejin.model.PaymentStatus;

public record PaymentStatusUpdateRequest(
        String correlationId,
        PaymentStatus status,
        String failureReason
) {
}
