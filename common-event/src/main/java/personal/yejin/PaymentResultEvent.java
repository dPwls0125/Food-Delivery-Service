package personal.yejin;

import java.time.LocalDateTime;


public record PaymentResultEvent(
        String correlationId,
        Long paymentId,
        LocalDateTime timestamp,
        Long orderId,
        Long userId,
        int amount,
//        String paymentMethod,
//        String pgResponseCode,
//        String pgTransactionId,
        String failureReason
) {
}
