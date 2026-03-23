package personal.yejin.foodDelivery.domain.order.dto;

public record OrderNearArrivalNotification(
        Long orderId,
        Long deliveryId,
        Long riderId,
        double remainingDistanceKm,
        String message
) {
    public static OrderNearArrivalNotification of(Long orderId, Long deliveryId, Long riderId, double remainingDistanceKm) {
        return new OrderNearArrivalNotification(
                orderId,
                deliveryId,
                riderId,
                remainingDistanceKm,
                "라이더가 곧 도착합니다."
        );
    }
}
