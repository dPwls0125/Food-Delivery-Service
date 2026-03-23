package personal.yejin.foodDelivery.domain.delivery.dto;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.model.DispatchStatus;

public record DispatchResponse(
        long orderId,
		long deliveryId,
        DispatchStatus dispatchStatus,
        DeliveryType deliveryType
) {


}
