package personal.yejin.foodDelivery.domain.delivery.dto;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;

public record DispatchRequest(DeliveryType deliveryType) {
}
