package personal.yejin.foodDelivery.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderItem;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;
import personal.yejin.foodDelivery.domain.order.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @DisplayName("가격 정보를 제외한 순수 주문 정보를 생성하고 저장한다.")
    @Test
    void createOrder_createsAndSavesOrder() {
        // Given
        Long storeId = 1L;
        List<OrderItem> orderItems = Arrays.asList(
                OrderItem.builder().menuId(101L).menuName("불고기버거").quantity(2).unitPrice(5000).build(),
                OrderItem.builder().menuId(102L).menuName("콜라").quantity(1).unitPrice(2000).build()
        );
        String deliveryAddress = "서울시 강남구 테헤란로 123";
        DeliveryType deliveryType = DeliveryType.BUNDLE;
        String customerNote = "문 앞에 놓아주세요";

        // Mocking behavior of orderRepository.save()
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(1L); // Simulate ID generation by JPA
            }
            return order;
        });

        // When
        Order createdOrder = orderService.createOrder(storeId, orderItems, deliveryAddress, deliveryType, customerNote);

        // Then
        assertNotNull(createdOrder.getId());
        assertEquals(storeId, createdOrder.getStoreId());
        assertEquals(deliveryAddress, createdOrder.getDeliveryAddress());
        assertEquals(deliveryType, createdOrder.getDeliveryType());
        assertEquals(OrderStatus.CREATED, createdOrder.getOrderStatus());
        assertEquals(customerNote, createdOrder.getCustomerNote());
        assertEquals(orderItems.size(), createdOrder.getOrderItems().size());

        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
