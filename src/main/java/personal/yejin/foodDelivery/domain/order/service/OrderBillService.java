package personal.yejin.foodDelivery.domain.order.service;

import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderItem;

import java.util.Optional;

@Service
public class OrderBillService {

    /**
     * 주문의 음식 가격, 배달비, 그리고 할인 금액을 계산하여 반환합니다.
     */
    public OrderBillResult calculateBill(Order order, Optional<Long> couponId, boolean useBaeminClub) {
        int foodPrice = calculateFoodPrice(order);
        int deliveryFee = calculateDeliveryFee(order);

        int totalDiscount = calculateDiscount(foodPrice, deliveryFee, couponId, useBaeminClub);
        int finalPrice = foodPrice + deliveryFee - totalDiscount;

        return new OrderBillResult(foodPrice, deliveryFee, totalDiscount, finalPrice);
    }

    private int calculateFoodPrice(Order order) {
        return order.getOrderItems().stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    private int calculateDeliveryFee(Order order) {
        // 배달 팁 로직 (임시 하드코딩, 실제로는 거리/배달 타입 등에 따라 달라짐)
        return order.getDeliveryType() != null && order.getDeliveryType().name().equals("BUNDLE") ? 3000 : 5000;
    }

    private int calculateDiscount(int foodPrice, int deliveryFee, Optional<Long> couponId, boolean useBaeminClub) {
        int discount = 0;

        // 1. 배민클럽 할인 (예: 배달비 무료 또는 10% 할인)
        if (useBaeminClub) {
            discount += deliveryFee; // 예시: 배달비 전액 할인
        }

        // 2. 쿠폰 할인 로직
        // 실제로는 CouponRepository 등을 통해 쿠폰을 조회하고 검증해야 함
        if (couponId.isPresent()) {
            // 쿠폰 사용 최소 금액 조건 (예: 15,000원)
            if (foodPrice >= 15000) {
                int couponDiscount = 3000; // 가짜 쿠폰 할인액
                // 최대 할인 금액 제한 (예: 5,000원)
                if (couponDiscount > 5000) {
                    couponDiscount = 5000;
                }
                discount += couponDiscount;
            } else {
                // 최소 주문 금액 미달 시 쿠폰 적용 안함 (또는 예외 발생)
                // throw new IllegalArgumentException("쿠폰 사용 최소 금액을 만족하지 못했습니다.");
            }
        }

        return discount;
    }

    public record OrderBillResult(int foodPrice, int deliveryFee, int totalDiscount, int finalPrice) {
    }
}
