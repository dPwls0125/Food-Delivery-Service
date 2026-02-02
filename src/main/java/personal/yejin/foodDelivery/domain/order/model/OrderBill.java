package personal.yejin.foodDelivery.domain.order.model;

import jakarta.persistence.*;
import lombok.*;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "order_bills")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderBill extends GlobalEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int foodPrice;
    private int deliveryFee;
    private int finalPrice;

    @Enumerated(EnumType.STRING)
    private OrderBillStatus status;

    @OneToMany(mappedBy = "orderBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppliedDiscount> appliedDiscounts = new ArrayList<>();

    @Builder
    public OrderBill(Order order, int foodPrice, int deliveryFee, OrderBillStatus status) {
        this.order = order;
        this.foodPrice = foodPrice;
        this.deliveryFee = deliveryFee;
        this.finalPrice = foodPrice + deliveryFee; // Initially finalPrice is sum of foodPrice and deliveryFee
        this.status = status;
    }

    public void addAppliedDiscount(AppliedDiscount appliedDiscount) {
        appliedDiscounts.add(appliedDiscount);
        appliedDiscount.setOrderBill(this);
    }
}
