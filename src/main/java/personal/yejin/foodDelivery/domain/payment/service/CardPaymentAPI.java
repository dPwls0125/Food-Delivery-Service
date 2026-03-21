package personal.yejin.foodDelivery.domain.payment.service;

import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;

public class CardPaymentAPI implements PaymentAPI {
    @Override
    public boolean pay(int amount, OrderPaymentRequest.PaymentMethod paymentMethod) {
        return true;
    }

    @Override
    public OrderPaymentRequest.PaymentMethod getSupportedMethod() {
        return OrderPaymentRequest.PaymentMethod.CARD;
    }
}
