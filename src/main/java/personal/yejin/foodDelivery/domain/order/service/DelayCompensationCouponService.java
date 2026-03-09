package personal.yejin.foodDelivery.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.order.model.DelayCompensationCoupon;
import personal.yejin.foodDelivery.domain.order.repository.DelayCompensationCouponRepository;

@Service
@RequiredArgsConstructor
public class DelayCompensationCouponService {

    private static final int DELAY_COMPENSATION_DISCOUNT_PERCENT = 15;
    private final DelayCompensationCouponRepository delayCompensationCouponRepository;

    @Transactional
    public void issueForDelayedDelivery(Long orderId) {
        if (delayCompensationCouponRepository.existsByOrderId(orderId)) {
            return;
        }

        DelayCompensationCoupon coupon = DelayCompensationCoupon.builder()
                .orderId(orderId)
                .discountPercent(DELAY_COMPENSATION_DISCOUNT_PERCENT)
                .reason("고지된 배달 시간 초과 보상 쿠폰")
                .build();

        delayCompensationCouponRepository.save(coupon);
    }
}
