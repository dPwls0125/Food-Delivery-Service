package personal.yejin.foodDelivery.domain.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderItem;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Long storeId, List<OrderItem> orderItems, String deliveryAddress, DeliveryType deliveryType, String customerNote) {
        Order newOrder = Order.builder()
                .storeId(storeId)
                .deliveryAddress(deliveryAddress)
                .deliveryType(deliveryType)
                .orderItems(orderItems)
                .orderStatus(OrderStatus.CREATED)
                .customerNote(customerNote)
                .build();

        return orderRepository.save(newOrder);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
    }
}
