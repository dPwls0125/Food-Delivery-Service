package personal.yejin.foodDelivery.domain.payment.service;

import org.springframework.stereotype.Component;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest.PaymentMethod;

@Component
public class FakePaymentClient implements PaymentClient {

    @Override
    public boolean pay(int amount, PaymentMethod paymentMethod) {
        // 현재는 결제가 항상 성공한다고 가정하는 가짜 구현체입니다.
        return true;
    }
}
