package personal.yejin.foodDelivery.domain.route.dto;

import personal.yejin.foodDelivery.domain.route.model.StopType;

public record Stop (
        int sequence,
        StopType type,
        long orderId,
        String address
){}
