package personal.yejin.foodDelivery.domain.payment.dto;

public record DiscountPreviewResponse(
        long orderId,
        int originalPrice,
        DiscountDetails discountDetails,
        int totalDiscountAmount,
        int finalPrice
){}
