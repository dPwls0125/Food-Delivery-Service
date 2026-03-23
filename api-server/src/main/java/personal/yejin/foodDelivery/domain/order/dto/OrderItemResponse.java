package personal.yejin.foodDelivery.domain.order.dto;

import lombok.Builder;

@Builder
public record OrderItemResponse(
        Long menuId,
        String menuName,
        int quantity,
        int price
) {
}
