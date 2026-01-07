package personal.yejin.foodDelivery.order.dto;

import lombok.Builder;

@Builder
public record OrderItemResponse(
        Long menuId,
        String menuName,
        int quantity,
        int price
) {
}
