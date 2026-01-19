package personal.yejin.foodDelivery.domain.order.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import lombok.ToString;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends GlobalEntity {
    private Long id;
    private Long storeId;
    private String deliveryAddress;
    private DeliveryType deliveryType;
    private List<OrderItem> orderItems;
    private OrderStatus orderStatus;
    private String customerNote; // 상세 조회 API에 포함된 고객 요청

    @Builder
    public Order(Long id, Long storeId, String deliveryAddress, DeliveryType deliveryType, List<OrderItem> orderItems,
            OrderStatus orderStatus, String customerNote) {
        this.id = id;
        this.storeId = storeId;
        this.deliveryAddress = deliveryAddress;
        this.deliveryType = deliveryType;
        this.orderItems = orderItems;
        this.orderStatus = orderStatus;
        this.customerNote = customerNote;
    }
}
