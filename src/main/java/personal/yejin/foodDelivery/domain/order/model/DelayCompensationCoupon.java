package personal.yejin.foodDelivery.domain.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Entity
@Table(name = "delay_compensation_coupons")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DelayCompensationCoupon extends GlobalEntity {

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private int discountPercent;

    @Column(nullable = false)
    private String reason;
}

