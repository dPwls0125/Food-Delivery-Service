package personal.yejin.foodDelivery.delivery.dto;

public record DispatchResponse(
        long orderId,
        DispatchStatus dispatchStatus,
        DispatchRequest.DeliveryType deliveryType
) {

    public enum DispatchStatus {
        REQUESTED,
        ACCEPTED,
        REJECTED
    }
}
