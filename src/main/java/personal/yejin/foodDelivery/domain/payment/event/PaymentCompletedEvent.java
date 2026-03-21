package personal.yejin.foodDelivery.domain.payment.event;

public record PaymentCompletedEvent(
        Long orderId,
        Long userId, // SSE 알림 대상을 식별하기 위함
        String correlationId, //  로깅이나 추적 용도 => 하나의 요청 흐름에 속한 로직들은 모두 동일한 correlationId를 가짐.
        Integer amount, // 이벤트 수신자(OrderService 등)가 결제 금액을 알 수 있도록
        String paymentMethod, 
        String paymentKey // PG사 승인 번호 등
) {}
