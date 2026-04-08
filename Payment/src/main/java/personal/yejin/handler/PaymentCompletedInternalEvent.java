package personal.yejin.handler;

import personal.yejin.model.PaymentStatus;

import java.time.LocalDateTime;

/**
 * Spring 내부 이벤트: PaymentRequestListener → PaymentEventHandler로 전달.
 */
public record PaymentCompletedInternalEvent(
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
