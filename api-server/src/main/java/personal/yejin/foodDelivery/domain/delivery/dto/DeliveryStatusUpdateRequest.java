package personal.yejin.foodDelivery.domain.delivery.dto;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;

public record DeliveryStatusUpdateRequest(DeliveryStatus status) {
}
