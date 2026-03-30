package personal.yejin.foodDelivery.domain.order.service;

import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderItem;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Long storeId, List<OrderItem> orderItems, String deliveryAddress,
            DeliveryType deliveryType, String customerNote) {
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

    // /**
    // * 결제 실패 이벤트를 수신하여 주문 상태를 업데이트(또는 취소 처리)합니다.
    // */
    // @org.springframework.kafka.annotation.KafkaListener(topics =
    // "payment-events", groupId = "order-group")
    // @org.springframework.transaction.annotation.Transactional
    // public void
    // handlePaymentFailedEvent(personal.yejin.foodDelivery.domain.payment.event.PaymentFailedEvent
    // event) {
    // Order order = getOrderById(event.orderId());
    //
    // order.changeStatus(OrderStatus.CANCELLED);
    //
    // System.out.println(String.format("[OrderService] 결제 실패로 인한 주문 취소! 주문번호: %d,
    // 사유: %s, 시도금액: %d",
    // event.orderId(), event.reason(), event.amount()));
    // }
    //
    // /**
    // * 결제 성공 이벤트를 수신하여 주문 상태를 즉시 업데이트합니다.
    // * Event-Carried State Transfer 패턴 적용: DB 조회 없이 페이로드의 결제 금액, 수단, 승인번호를 활용
    // */
    // @org.springframework.kafka.annotation.KafkaListener(topics =
    // "payment-events", groupId = "order-group")
    // @org.springframework.transaction.annotation.Transactional
    // public void
    // handlePaymentCompletedEvent(personal.yejin.foodDelivery.domain.payment.event.PaymentCompletedEvent
    // event) {
    // Order order = getOrderById(event.orderId());
    //
    // // 상태 변경 (Order 엔티티에 changeStatus 메서드가 있다고 가정, 없다면 추가 필요)
    // // 향후 빌링 내역 저장 등 후속 작업 시 event.amount(), event.paymentMethod(),
    // event.paymentKey() 등을 적극 활용합니다.
    // order.changeStatus(OrderStatus.PAID);
    // // orderRepository.save(order); // JPA 더티 체킹 활용
    //
    // System.out.println(String.format("[OrderService] 결제 완료 상태 업데이트! 주문번호: %d,
    // 결제금액: %d, 결제수단: %s, 승인번호: %s",
    // event.orderId(), event.amount(), event.paymentMethod(), event.paymentKey()));
    // }
}
