package personal.yejin.foodDelivery.domain.payment.service;

import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest.PaymentMethod;

public interface PaymentClient {
    boolean pay(int amount, PaymentMethod paymentMethod);
}
