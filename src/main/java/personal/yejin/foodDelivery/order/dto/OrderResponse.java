package personal.yejin.foodDelivery.order.dto;

import lombok.Builder;
import personal.yejin.foodDelivery.order.model.OrderStatus;

import java.util.List;

@Builder
public record OrderResponse(
        Long orderId,
        OrderStatus orderStatus,
        int totalPrice,
        String deliveryAddress,
        List<OrderItemResponse> orderItems
) {
}
