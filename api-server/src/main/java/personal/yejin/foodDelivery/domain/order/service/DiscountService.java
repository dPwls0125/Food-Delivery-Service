package personal.yejin.foodDelivery.domain.order.service;

import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.order.model.Order;

@Service
public class DiscountService {

    /**
     * 주문에 대한 할인 금액을 계산합니다.
     * TODO: 실제 쿠폰 조회 및 배민클럽 로직 구현
     */
    public int calculateDiscount(Order order, Long couponId, Boolean useBaeminClub) {
        int discount = 0;

        if (couponId != null) {
            discount += 3000; // TODO: 쿠폰 DB 조회 후 실제 할인 금액 적용
        }
        if (Boolean.TRUE.equals(useBaeminClub)) {
            discount += 1500; // TODO: 배민클럽 할인 정책 적용
        }

        int orderTotalAmount = order.getFoodPrice() + order.getDeliveryFee();
        return Math.min(discount,orderTotalAmount);
    }
}
