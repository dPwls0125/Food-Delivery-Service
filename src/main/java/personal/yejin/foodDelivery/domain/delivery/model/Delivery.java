package personal.yejin.foodDelivery.domain.delivery.model;

import lombok.Builder;
import lombok.Getter;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.route.model.Route;

@Getter
public class Delivery extends GlobalEntity {

    private final Order order;
    private Rider rider;
    private DeliveryStatus status;
    private final DeliveryType deliveryType;
    private Route route;

    @Builder
    public Delivery(Order order, DeliveryType deliveryType) {
        this.id = id;
        this.order = order;
        this.status = DeliveryStatus.PENDING; // 초기 상태
        this.deliveryType = deliveryType;
    }

    public void dispatch(Route route) {
        this.route = route;
        this.rider = route.getRider();
        this.status = DeliveryStatus.DISPATCHED;
    }
}
