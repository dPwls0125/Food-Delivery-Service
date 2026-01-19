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
public class AppliedDiscount extends GlobalEntity {
    private Long id;
    private Long orderBillId; // The OrderBill this discount belongs to
    private AppliedDiscountType discountType;
    private int amount;
    private Long sourceId; // e.g., couponId, can be null for BaeminClub
    private String description; // e.g., "10% 할인 쿠폰", "배민클럽 할인"

    @Builder
    public AppliedDiscount(Long id, Long orderBillId, AppliedDiscountType discountType, int amount, Long sourceId, String description) {
        this.id = id;
        this.orderBillId = orderBillId;
        this.discountType = discountType;
        this.amount = amount;
        this.sourceId = sourceId;
        this.description = description;
    }
}
