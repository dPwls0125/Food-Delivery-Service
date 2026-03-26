package personal.yejin.foodDelivery.domain.order.dto;

import personal.yejin.model.PaymentMethod;

public record OrderPaymentRequest(
        PaymentMethod paymentMethod,
        Long couponId,
        Boolean useBaeminClub) {
}
