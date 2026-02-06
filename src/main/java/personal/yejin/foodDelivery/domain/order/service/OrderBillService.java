package personal.yejin.foodDelivery.domain.order.service;

import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.*;
import personal.yejin.foodDelivery.domain.order.repository.AppliedDiscountRepository;
import personal.yejin.foodDelivery.domain.order.repository.OrderBillRepository;

import java.util.Optional;

@Service
public class OrderBillService {

    private final OrderBillRepository orderBillRepository;
    private final AppliedDiscountRepository appliedDiscountRepository;

    public OrderBillService(OrderBillRepository orderBillRepository, AppliedDiscountRepository appliedDiscountRepository) {
        this.orderBillRepository = orderBillRepository;
        this.appliedDiscountRepository = appliedDiscountRepository;
    }

    public OrderBill createPreviewBill(Order order, Optional<Long> couponId, boolean useBaeminClub) {
        // 1. Calculate food price
        int foodPrice = order.getOrderItems().stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        // 2. Determine delivery fee (hardcoded for now)
        int deliveryFee = getDeliveryFee(order.getDeliveryType());

        // 3. Create OrderBill instance first
        OrderBill previewBill = OrderBill.builder()
                .foodPrice(foodPrice)
                .deliveryFee(deliveryFee)
                .status(OrderBillStatus.PREVIEW)
                .build();

        // 4. Calculate discounts and associate with the bill
        int foodDiscount = 0; // TODO : 쿠폰 객체 생성
        int deliveryDiscount = 0; // TODO : 쿠폰 객체 생성

        if (couponId.isPresent()) {
            int couponDiscountAmount = 3000; // Example fixed discount
            foodDiscount += couponDiscountAmount;
            previewBill.addAppliedDiscount(AppliedDiscount.builder()
                    .discountType(AppliedDiscountType.COUPON)
                    .amount(couponDiscountAmount)
                    .sourceId(couponId.get())
                    .description("음식 할인 쿠폰")
                    .build());
        }

        if (useBaeminClub) {
            int baeminClubDiscountAmount = (int) (deliveryFee * 0.1); // 10% of delivery fee
            deliveryDiscount += baeminClubDiscountAmount;
            previewBill.addAppliedDiscount(AppliedDiscount.builder()
                    .discountType(AppliedDiscountType.BAEMIN_CLUB)
                    .amount(baeminClubDiscountAmount)
                    .description("배민클럽 배달비 할인")
                    .build());
        }

        // 5. Set final price and save the bill with its discounts
        int finalPrice = (foodPrice - foodDiscount) + (deliveryFee - deliveryDiscount);
        previewBill.setFinalPrice(finalPrice);

        return orderBillRepository.save(previewBill);
    }

    private int getDeliveryFee(DeliveryType deliveryType) {
        // In a real application, this would come from a configuration or a more complex service
        if (deliveryType == DeliveryType.BUNDLE) {
            return 3000;
        } else {
            return 5000;
        }
    }
}
