package personal.yejin.foodDelivery.payment.dto;

public record DiscountPreviewResponse(
        long orderId,
        int originalPrice,
        DiscountDetails discountDetails,
        int totalDiscountAmount,
        int finalPrice
){}
