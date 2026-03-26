package personal.yejin.foodDelivery.domain.rider.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RiderDispatchNotification {
    private Long riderId;
    private Long deliveryId;
    private Long orderId;
    private DeliveryType deliveryType;

    public static RiderDispatchNotification of(Long riderId, Long deliveryId, Long orderId, DeliveryType deliveryType){
        return new RiderDispatchNotification(riderId, deliveryId, orderId, deliveryType);
    }
}
