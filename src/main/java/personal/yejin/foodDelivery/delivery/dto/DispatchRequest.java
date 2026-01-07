package personal.yejin.foodDelivery.delivery.dto;

public record DispatchRequest(DeliveryType deliveryType) {
    public enum DeliveryType {
        SINGLE, BUNDLE
    }
}
