package personal.yejin;

import personal.yejin.model.PaymentStatus;

import java.time.LocalDateTime;


public record PaymentResultEvent(
        String correlationId,
        Long paymentId,
        Long orderId,
        Long userId,
        Long storeId,
        PaymentStatus paymentStatus,
        LocalDateTime timestamp,
        int amount,
        String failureReason
) {
}
