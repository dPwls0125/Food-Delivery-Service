package personal.yejin.foodDelivery.delivery.dto;

import personal.yejin.foodDelivery.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.delivery.model.DispatchStatus;

public record DispatchResponse(
        long orderId,
        DispatchStatus dispatchStatus,
        DeliveryType deliveryType
) {


}
