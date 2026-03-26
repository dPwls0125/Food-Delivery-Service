package personal.yejin.service;
import org.springframework.stereotype.Component;
import personal.yejin.model.PaymentMethod;


@Component
public class CashPaymentAPI implements PaymentAPI {
    @Override
    public boolean pay(int amount, PaymentMethod paymentMethod) {
        return true;
    }

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH;
    }
}
