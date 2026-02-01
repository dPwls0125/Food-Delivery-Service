package personal.yejin.foodDelivery.domain.order.dto;

import lombok.Builder;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;

@Builder
public record OrderCreateResponse(
        Long orderId,
        OrderStatus orderStatus,
        int totalPrice
){ }
