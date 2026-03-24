package personal.yejin.service;
import personal.yejin.model.PaymentMethod;

public class CashPaymentAPI implements PaymentAPI {
    @Override
    public boolean pay(int amount, PaymentMethod paymentMethod) {
        return false;
    }

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH;
    }
}
