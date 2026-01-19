package personal.yejin.foodDelivery.rider.dto;

import java.util.List;
import lombok.Builder;
import personal.yejin.foodDelivery.order.model.OrderStatus;

@Builder
public record RiderOrderDetailResponse(
    long orderId,
    StoreInfo store,
    String deliveryAddress,
    OrderStatus orderStatus,
    List<ItemInfo> items,
    String customerNote
) {
    @Builder
    public record StoreInfo(
        String name,
        String address
    ) {}

    @Builder
    public record ItemInfo(
        String name,
        int quantity
    ) {}
}
