package personal.yejin.foodDelivery.order.dto;

import lombok.Builder;
import personal.yejin.foodDelivery.order.model.OrderStatus;

@Builder
public record OrderCreateResponse(
        Long orderId,
        OrderStatus orderStatus,
        int totalPrice
){ }
