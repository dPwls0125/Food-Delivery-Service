package personal.yejin.service;


import personal.yejin.model.PaymentMethod;

public interface PaymentAPI {
    boolean pay(int amount, PaymentMethod paymentMethod);
    PaymentMethod getSupportedMethod();
}
