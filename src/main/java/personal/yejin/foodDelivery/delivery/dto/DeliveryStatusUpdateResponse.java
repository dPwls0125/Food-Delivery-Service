package personal.yejin.foodDelivery.delivery.dto;

import personal.yejin.foodDelivery.delivery.model.DeliveryStatus;

public record DeliveryStatusUpdateResponse(
        long orderId,
        DeliveryStatus updatedStatus
){}
