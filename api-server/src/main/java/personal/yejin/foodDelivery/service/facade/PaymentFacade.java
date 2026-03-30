package personal.yejin.foodDelivery.service.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.service.OrderService;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.dto.PaymentResponse;
import personal.yejin.foodDelivery.exception.PaymentException;
import personal.yejin.foodDelivery.domain.order.service.DiscountService;
import personal.yejin.foodDelivery.service.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DiscountService discountService;

    public PaymentResponse processPayment(long orderId, long userId, OrderPaymentRequest request) {
        Order order = orderService.getOrderById(orderId);

        if (order.getUserId() != userId) {
            throw new PaymentException("주문 정보와 사용자 정보가 일치하지 않습니다.", null);
        }

        int originalPrice = order.getFoodPrice() + order.getDeliveryFee();
        int discount = discountService.calculateDiscount(order, request.couponId(), request.useBaeminClub());
        int finalPrice = originalPrice - discount;

        return paymentService.processPayment(
                orderId,
                userId,
                finalPrice,
                request.paymentMethod());
    }
}
