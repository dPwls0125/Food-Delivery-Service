package personal.yejin.foodDelivery.delivery.dto;

public record DeliveryStatusUpdateResponse(
        long orderId,
        DeliveryStatusUpdateRequest.DeliveryStatus updatedStatus
){}
