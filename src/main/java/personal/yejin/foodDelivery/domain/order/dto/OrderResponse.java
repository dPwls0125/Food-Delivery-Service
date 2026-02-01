package personal.yejin.foodDelivery.domain.order.dto;

import lombok.Builder;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;

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
