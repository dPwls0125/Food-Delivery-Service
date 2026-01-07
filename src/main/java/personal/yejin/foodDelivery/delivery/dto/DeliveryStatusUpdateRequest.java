package personal.yejin.foodDelivery.delivery.dto;

public record DeliveryStatusUpdateRequest(DeliveryStatus status) {
    public enum DeliveryStatus {
        PICKED_UP,
        DELIVERED_TO_CUSTOMER
    }
}
