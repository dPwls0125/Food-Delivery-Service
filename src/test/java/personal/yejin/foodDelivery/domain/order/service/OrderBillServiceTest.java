//package personal.yejin.foodDelivery.domain.order.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
//import personal.yejin.foodDelivery.domain.order.model.*;
//import personal.yejin.foodDelivery.domain.order.repository.AppliedDiscountRepository;
//import personal.yejin.foodDelivery.domain.order.repository.OrderBillRepository;
//import personal.yejin.foodDelivery.domain.order.service.OrderBillService;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderBillServiceTest {
//
//    @Mock
//    private OrderBillRepository orderBillRepository;
//    @Mock
//    private AppliedDiscountRepository appliedDiscountRepository;
//
//    @InjectMocks
//    private OrderBillService orderBillService;
//
//    private Order sampleOrder;
//
//    @BeforeEach
//    void setUp() {
//        // Common sample order for all tests
//        List<OrderItem> orderItems = Arrays.asList(
//                OrderItem.builder().menuId(101L).menuName("불고기버거").quantity(2).unitPrice(5000).build(), // 10000
//                OrderItem.builder().menuId(102L).menuName("콜라").quantity(1).unitPrice(2000).build()      // 2000
//        );
//        sampleOrder = Order.builder()
//                .id(1L) // Add ID for the order
//                .storeId(100L)
//                .deliveryType(DeliveryType.BUNDLE) // BUNDLE fee is 3000
//                .orderItems(orderItems)
//                .orderStatus(OrderStatus.CREATED)
//                .build();
//    }
//
//    @DisplayName("쿠폰 할인을 적용하여 미리보기 계산서를 생성한다 (음식값 할인)")
//    @Test
//    void createPreviewBill_withCouponOnly() {
//        // Given
//        Long couponId = 10L;
//        int foodPrice = 12000;
//        int deliveryFee = 3000;
//        int couponDiscount = 3000;
//
//
//
//
//        when(orderBillRepository.save(any(OrderBill.class))).thenAnswer(invocation -> {
//            OrderBill bill = invocation.getArgument(0);
//            if (bill.getId() == null) {
//                bill.setId(1L); // Simulate ID generation
//            }
//            return bill;
//        });
//
//        // When
//        OrderBill previewBill = orderBillService.createPreviewBill(sampleOrder, Optional.of(couponId), false);
//
//        // Then
//        assertEquals(foodPrice, previewBill.getFoodPrice());
//        assertEquals(deliveryFee, previewBill.getDeliveryFee());
//        assertEquals(foodPrice - couponDiscount + deliveryFee, previewBill.getFinalPrice());
//
//        assertEquals(1, previewBill.getAppliedDiscounts().size());
//        assertEquals(AppliedDiscountType.COUPON, previewBill.getAppliedDiscounts().get(0).getDiscountType());
//        assertEquals(couponDiscount, previewBill.getAppliedDiscounts().get(0).getAmount());
//
//        verify(orderBillRepository, times(1)).save(any(OrderBill.class));
//    }
//
//    @DisplayName("배민클럽 할인을 적용하여 미리보기 계산서를 생성한다 (배달비 할인)")
//    @Test
//    void createPreviewBill_withBaeminClubOnly() {
//        // Given
//        int foodPrice = 12000;
//        int deliveryFee = 3000;
//        int baeminClubDiscount = (int) (deliveryFee * 0.1);
//
//
//
//
//        when(orderBillRepository.save(any(OrderBill.class))).thenAnswer(invocation -> {
//            OrderBill bill = invocation.getArgument(0);
//            if (bill.getId() == null) {
//                bill.setId(1L); // Simulate ID generation
//            }
//            return bill;
//        });
//
//        // When
//        OrderBill previewBill = orderBillService.createPreviewBill(sampleOrder, Optional.empty(), true);
//
//        // Then
//        assertEquals(foodPrice, previewBill.getFoodPrice());
//        assertEquals(deliveryFee, previewBill.getDeliveryFee());
//        assertEquals(foodPrice + (deliveryFee - baeminClubDiscount), previewBill.getFinalPrice());
//
//        assertEquals(1, previewBill.getAppliedDiscounts().size());
//        assertEquals(AppliedDiscountType.BAEMIN_CLUB, previewBill.getAppliedDiscounts().get(0).getDiscountType());
//        assertEquals(baeminClubDiscount, previewBill.getAppliedDiscounts().get(0).getAmount());
//
//        verify(orderBillRepository, times(1)).save(any(OrderBill.class));
//    }
//
//    @DisplayName("쿠폰과 배민클럽 할인을 모두 적용하여 미리보기 계산서를 생성한다")
//    @Test
//    void createPreviewBill_withBothDiscounts() {
//        // Given
//        Long couponId = 10L;
//        int foodPrice = 12000;
//        int deliveryFee = 3000;
//        int couponDiscount = 3000;
//        int baeminClubDiscount = (int) (deliveryFee * 0.1);
//
//        when(orderBillRepository.save(any(OrderBill.class))).thenAnswer(invocation -> {
//            OrderBill bill = invocation.getArgument(0);
//            if (bill.getId() == null) {
//                bill.setId(1L); // Simulate ID generation
//            }
//            return bill;
//        });
//
//        // When
//        OrderBill previewBill = orderBillService.createPreviewBill(sampleOrder, Optional.of(couponId), true);
//
//        // Then
//        assertEquals(foodPrice, previewBill.getFoodPrice());
//        assertEquals(deliveryFee, previewBill.getDeliveryFee());
//        assertEquals((foodPrice - couponDiscount) + (deliveryFee - baeminClubDiscount), previewBill.getFinalPrice());
//
//        assertEquals(2, previewBill.getAppliedDiscounts().size());
//        assertTrue(previewBill.getAppliedDiscounts().stream().anyMatch(d -> d.getDiscountType() == AppliedDiscountType.COUPON && d.getAmount() == couponDiscount));
//        assertTrue(previewBill.getAppliedDiscounts().stream().anyMatch(d -> d.getDiscountType() == AppliedDiscountType.BAEMIN_CLUB && d.getAmount() == baeminClubDiscount));
//
//        verify(orderBillRepository, times(1)).save(any(OrderBill.class));
//    }
//}
