package personal.yejin.foodDelivery.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderItem;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.InMemoryOrderRepository;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepository();
        orderService = new OrderService(orderRepository);
    }

    @DisplayName("가격 정보를 제외한 순수 주문 정보를 생성하고 저장한다.")
    @Test
    void createOrder_createsAndSavesOrder() {
        // Given
        Long storeId = 1L;
        List<OrderItem> orderItems = Arrays.asList(
                new OrderItem(101L, "불고기버거", 2, 5000),
                new OrderItem(102L, "콜라", 1, 2000)
        );
        String deliveryAddress = "서울시 강남구 테헤란로 123";
        DeliveryType deliveryType = DeliveryType.BUNDLE;
        String customerNote = "문 앞에 놓아주세요";

        // When
        Order createdOrder = orderService.createOrder(storeId, orderItems, deliveryAddress, deliveryType, customerNote);

        // Then
        // Verify properties of the created order
        assertNotNull(createdOrder.getId());
        assertEquals(storeId, createdOrder.getStoreId());
        assertEquals(deliveryAddress, createdOrder.getDeliveryAddress());
        assertEquals(deliveryType, createdOrder.getDeliveryType());
        assertEquals(OrderStatus.CREATED, createdOrder.getOrderStatus());
        assertEquals(customerNote, createdOrder.getCustomerNote());
        assertEquals(orderItems.size(), createdOrder.getOrderItems().size());
        
        // Verify timestamps are set by the repository
        assertNotNull(createdOrder.getCreatedAt());
        assertNotNull(createdOrder.getUpdatedAt());

        // Verify that the order is actually saved in the repository
        Optional<Order> foundOrderOptional = orderRepository.findById(createdOrder.getId());
        assertTrue(foundOrderOptional.isPresent());
        Order foundOrder = foundOrderOptional.get();

        assertEquals(createdOrder, foundOrder);
        // Also check timestamps on the retrieved object
        assertNotNull(foundOrder.getCreatedAt());
        assertNotNull(foundOrder.getUpdatedAt());
    }
}
