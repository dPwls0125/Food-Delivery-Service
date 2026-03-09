package personal.yejin.foodDelivery.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.foodDelivery.domain.order.model.DelayCompensationCoupon;

public interface DelayCompensationCouponRepository extends JpaRepository<DelayCompensationCoupon, Long> {
    boolean existsByOrderId(Long orderId);
}
