package personal.yejin.foodDelivery.domain.order.dto;
import java.util.List;
public record OrderCreateRequest(
        Long storeId,
        List<OrderItemRequest> orderItems,
        String deliveryAddress
) {
    public record OrderItemRequest(
            Long menuId,
            int quantity
    ){}
}
