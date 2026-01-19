package personal.yejin.foodDelivery.domain.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.*;
import personal.yejin.foodDelivery.domain.order.repository.AppliedDiscountRepository;
import personal.yejin.foodDelivery.domain.order.repository.OrderBillRepository;

import java.util.ArrayList;
import java.util.List;
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

        // 3. Calculate discounts
        List<AppliedDiscount> appliedDiscounts = new ArrayList<>();
        int foodDiscount = 0;
        int deliveryDiscount = 0;

        // 3a. Coupon discount applies to food price
        if (couponId.isPresent()) {
            // In a real scenario, fetch coupon details and validate rules
            int couponDiscountAmount = 3000; // Example fixed discount
            foodDiscount += couponDiscountAmount;
            appliedDiscounts.add(AppliedDiscount.builder()
                    .discountType(AppliedDiscountType.COUPON)
                    .amount(couponDiscountAmount)
                    .sourceId(couponId.get())
                    .description("음식 할인 쿠폰")
                    .build());
        }

        // 3b. Baemin Club discount applies to delivery fee
        if (useBaeminClub) {
            int baeminClubDiscountAmount = (int) (deliveryFee * 0.1); // 10% of delivery fee
            deliveryDiscount += baeminClubDiscountAmount;
            appliedDiscounts.add(AppliedDiscount.builder()
                    .discountType(AppliedDiscountType.BAEMIN_CLUB)
                    .amount(baeminClubDiscountAmount)
                    .description("배민클럽 배달비 할인")
                    .build());
        }

        // 4. Create and save OrderBill
        int finalPrice = (foodPrice - foodDiscount) + (deliveryFee - deliveryDiscount);

        OrderBill previewBill = OrderBill.builder()
                .orderId(order.getId())
                .foodPrice(foodPrice)
                .deliveryFee(deliveryFee)
                .status(OrderBillStatus.PREVIEW)
                .build();
        previewBill.setFinalPrice(finalPrice);

        OrderBill savedOrderBill = orderBillRepository.save(previewBill);

        // 5. Associate discounts with the saved bill and save them
        for (AppliedDiscount discount : appliedDiscounts) {
            discount.setOrderBillId(savedOrderBill.getId());
            appliedDiscountRepository.save(discount);
        }

        return savedOrderBill;
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
