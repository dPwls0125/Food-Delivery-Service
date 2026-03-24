package personal.yejin.service;

import personal.yejin.model.PaymentMethod;

public class CardPaymentAPI implements PaymentAPI {

    @Override
    public boolean pay(int amount, PaymentMethod paymentMethod) {
        return true;
    }

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CARD;
    }
}
