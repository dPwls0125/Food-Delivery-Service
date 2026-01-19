package personal.yejin.foodDelivery.domain.order.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderBill extends GlobalEntity {
    private Long id;
    private Long orderId;
    private int foodPrice;
    private int deliveryFee;
    private int finalPrice;
    private OrderBillStatus status;

    @Builder
    public OrderBill(Long id, Long orderId, int foodPrice, int deliveryFee, OrderBillStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.foodPrice = foodPrice;
        this.deliveryFee = deliveryFee;
        this.finalPrice = foodPrice + deliveryFee; // Initially finalPrice is sum of foodPrice and deliveryFee
        this.status = status;
    }
}
