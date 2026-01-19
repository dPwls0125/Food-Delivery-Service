package personal.yejin.foodDelivery.domain.delivery.dto;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.model.DispatchStatus;

public record DispatchResponse(
        long orderId,
        DispatchStatus dispatchStatus,
        DeliveryType deliveryType
) {


}
