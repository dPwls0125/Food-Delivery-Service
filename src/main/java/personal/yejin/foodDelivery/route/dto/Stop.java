package personal.yejin.foodDelivery.route.dto;

import personal.yejin.foodDelivery.route.model.StopType;

public record Stop (
        int sequence,
        StopType type,
        long orderId,
        String address
){}
