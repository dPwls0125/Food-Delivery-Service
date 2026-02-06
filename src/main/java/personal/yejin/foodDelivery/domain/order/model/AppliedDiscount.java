package personal.yejin.foodDelivery.domain.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Getter
@Setter
@Entity
@Table(name = "applied_discounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppliedDiscount extends GlobalEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_bill_id")
    @JsonIgnore
    private OrderBill orderBill;

    @Enumerated(EnumType.STRING)
    private AppliedDiscountType discountType;

    private int amount;
    private Long sourceId; // e.g., couponId, can be null for BaeminClub
    private String description; // e.g., "10% 할인 쿠폰", "배민클럽 할인"

    @Builder
    public AppliedDiscount(OrderBill orderBill, AppliedDiscountType discountType, int amount, Long sourceId, String description) {
        this.orderBill = orderBill;
        this.discountType = discountType;
        this.amount = amount;
        this.sourceId = sourceId;
        this.description = description;
    }

    public void setOrderBill(OrderBill orderBill) {
        this.orderBill = orderBill;
        if (!orderBill.getAppliedDiscounts().contains(this)) {
            orderBill.getAppliedDiscounts().add(this);
        }
    }
}
