package personal.yejin.foodDelivery.domain.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.*;
import personal.yejin.foodDelivery.domain.order.repository.AppliedDiscountRepository;
import personal.yejin.foodDelivery.domain.order.repository.InMemoryAppliedDiscountRepository;
import personal.yejin.foodDelivery.domain.order.repository.InMemoryOrderBillRepository;
import personal.yejin.foodDelivery.domain.order.repository.OrderBillRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderBillServiceTest {

    private OrderBillRepository orderBillRepository;
    private AppliedDiscountRepository appliedDiscountRepository;
    private OrderBillService orderBillService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        orderBillRepository = new InMemoryOrderBillRepository();
        appliedDiscountRepository = new InMemoryAppliedDiscountRepository();
        orderBillService = new OrderBillService(orderBillRepository, appliedDiscountRepository);

        // Common sample order for all tests
        List<OrderItem> orderItems = Arrays.asList(
                new OrderItem(101L, "불고기버거", 2, 5000), // 10000
                new OrderItem(102L, "콜라", 1, 2000)      // 2000
        );
        sampleOrder = Order.builder()
                .id(1L)
                .storeId(100L)
                .deliveryType(DeliveryType.BUNDLE) // BUNDLE fee is 3000
                .orderItems(orderItems)
                .orderStatus(OrderStatus.CREATED)
                .build();
    }

    @DisplayName("쿠폰 할인을 적용하여 미리보기 계산서를 생성한다 (음식값 할인)")
    @Test
    void createPreviewBill_withCouponOnly() {
        // Given
        Long couponId = 10L;
        int foodPrice = 12000;
        int deliveryFee = 3000;
        int couponDiscount = 3000;

        // When
        OrderBill previewBill = orderBillService.createPreviewBill(sampleOrder, Optional.of(couponId), false);

        // Then
        assertEquals(foodPrice, previewBill.getFoodPrice());
        assertEquals(deliveryFee, previewBill.getDeliveryFee());
        assertEquals(foodPrice - couponDiscount + deliveryFee, previewBill.getFinalPrice());

        List<AppliedDiscount> discounts = appliedDiscountRepository.findByOrderBillId(previewBill.getId());
        assertEquals(1, discounts.size());
        assertEquals(AppliedDiscountType.COUPON, discounts.get(0).getDiscountType());
        assertEquals(couponDiscount, discounts.get(0).getAmount());
    }

    @DisplayName("배민클럽 할인을 적용하여 미리보기 계산서를 생성한다 (배달비 할인)")
    @Test
    void createPreviewBill_withBaeminClubOnly() {
        // Given
        int foodPrice = 12000;
        int deliveryFee = 3000;
        int baeminClubDiscount = (int) (deliveryFee * 0.1);

        // When
        OrderBill previewBill = orderBillService.createPreviewBill(sampleOrder, Optional.empty(), true);

        // Then
        assertEquals(foodPrice, previewBill.getFoodPrice());
        assertEquals(deliveryFee, previewBill.getDeliveryFee());
        assertEquals(foodPrice + (deliveryFee - baeminClubDiscount), previewBill.getFinalPrice());

        List<AppliedDiscount> discounts = appliedDiscountRepository.findByOrderBillId(previewBill.getId());
        assertEquals(1, discounts.size());
        assertEquals(AppliedDiscountType.BAEMIN_CLUB, discounts.get(0).getDiscountType());
        assertEquals(baeminClubDiscount, discounts.get(0).getAmount());
    }

    @DisplayName("쿠폰과 배민클럽 할인을 모두 적용하여 미리보기 계산서를 생성한다")
    @Test
    void createPreviewBill_withBothDiscounts() {
        // Given
        Long couponId = 10L;
        int foodPrice = 12000;
        int deliveryFee = 3000;
        int couponDiscount = 3000;
        int baeminClubDiscount = (int) (deliveryFee * 0.1);

        // When
        OrderBill previewBill = orderBillService.createPreviewBill(sampleOrder, Optional.of(couponId), true);

        // Then
        assertEquals(foodPrice, previewBill.getFoodPrice());
        assertEquals(deliveryFee, previewBill.getDeliveryFee());
        assertEquals((foodPrice - couponDiscount) + (deliveryFee - baeminClubDiscount), previewBill.getFinalPrice());

        List<AppliedDiscount> discounts = appliedDiscountRepository.findByOrderBillId(previewBill.getId());
        assertEquals(2, discounts.size());
        assertTrue(discounts.stream().anyMatch(d -> d.getDiscountType() == AppliedDiscountType.COUPON && d.getAmount() == couponDiscount));
        assertTrue(discounts.stream().anyMatch(d -> d.getDiscountType() == AppliedDiscountType.BAEMIN_CLUB && d.getAmount() == baeminClubDiscount));
    }
}
