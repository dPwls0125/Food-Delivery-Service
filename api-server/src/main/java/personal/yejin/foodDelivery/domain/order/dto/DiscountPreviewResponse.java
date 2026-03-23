package personal.yejin.foodDelivery.domain.order.dto;

import personal.yejin.foodDelivery.domain.payment.dto.DiscountDetails;

public record DiscountPreviewResponse(
        long orderId,
        int originalPrice,
        DiscountDetails discountDetails,
        int totalDiscountAmount,
        int finalPrice
){}
