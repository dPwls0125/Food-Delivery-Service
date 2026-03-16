package personal.yejin.foodDelivery.domain.payment.event;

public record PaymentCompletedEvent(
        Long orderId,
        Long userId, // SSE 알림 대상을 식별하기 위함
        String correlationId // (선택) 로깅이나 추적용도
) {}
