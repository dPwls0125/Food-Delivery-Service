package personal.yejin.foodDelivery.domain.order.model;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.rider.model.Location;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Order extends GlobalEntity {
    private Long storeId;
    private Location pickupLocation; // 픽업 위치
    private String deliveryAddress;
    private Location deliveryLocation; // 배달 위치
    private DeliveryType deliveryType;
    private List<OrderItem> orderItems;
    private OrderStatus orderStatus;
    private String customerNote; // 상세 조회 API에 포함된 고객 요청
}
