package personal.yejin.foodDelivery.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentResponse;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;
import personal.yejin.foodDelivery.domain.order.service.OrderBillService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderBillService orderBillService;
    private final PaymentClient paymentClient;

    @Transactional
    public OrderPaymentResponse processPayment(Long orderId, OrderPaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("주문이 결제 대기 상태가 아닙니다.");
        }

        // 1. 주문 금액 및 할인 계산 (OrderBillService 위임)
        Optional<Long> couponId = request.couponId() > 0 ? Optional.of(request.couponId()) : Optional.empty();
        OrderBillService.OrderBillResult billResult = orderBillService.calculateBill(order, couponId, request.useBaeminClub());

        order.updatePrices(billResult.foodPrice(), billResult.deliveryFee(), billResult.totalDiscount());

        // 2. 외부 결제 API 호출
        boolean paymentSuccess = paymentClient.pay(order.getFinalPrice(), request.paymentMethod());

        if (!paymentSuccess) {
            throw new RuntimeException("결제에 실패했습니다.");
        }

        // 3. 결제 성공 시 상태 변경
        order.setOrderStatus(OrderStatus.PAID);

        int totalDiscount = order.getFoodPrice() + order.getDeliveryFee() - order.getFinalPrice();

        return new OrderPaymentResponse(
                order.getId(),
                order.getOrderStatus(),
                OrderPaymentResponse.PaymentStatus.SUCCESS,
                order.getFoodPrice() + order.getDeliveryFee(),
                totalDiscount,
                order.getFinalPrice(),
                request.paymentMethod(),
                LocalDateTime.now());
    }
}
