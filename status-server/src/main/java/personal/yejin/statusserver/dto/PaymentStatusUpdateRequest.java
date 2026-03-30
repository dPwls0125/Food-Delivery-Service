package personal.yejin.statusserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import personal.yejin.model.PaymentStatus;

public record PaymentStatusUpdateRequest(
        @NotBlank String correlationId,
        @NotNull PaymentStatus status,
        String failureReason
) {
}
