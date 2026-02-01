package personal.yejin.foodDelivery.domain.delivery.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.route.model.Route;

@Getter
@SuperBuilder
public class Delivery extends GlobalEntity {

    private final Order order;
    private Rider rider;
    private DeliveryStatus status;
    private final DeliveryType deliveryType;
    private Route route;

    public void dispatch(Route route) {
        this.route = route;
        this.rider = route.getRider();
        this.status = DeliveryStatus.DISPATCHED;
    }

    // 테스트 및 필요에 따라 상태를 설정하기 위한 메서드 추가 (주로 테스트 용도)
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
}
