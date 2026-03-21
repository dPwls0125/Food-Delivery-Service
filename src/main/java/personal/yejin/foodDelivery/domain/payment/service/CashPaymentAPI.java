package personal.yejin.foodDelivery.domain.payment.service;

import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;

public class CashPaymentAPI implements PaymentAPI{
    @Override
    public boolean pay(int amount, OrderPaymentRequest.PaymentMethod paymentMethod) {
        return false;
    }

    @Override
    public OrderPaymentRequest.PaymentMethod getSupportedMethod() {
        return OrderPaymentRequest.PaymentMethod.CASH;
    }
}
