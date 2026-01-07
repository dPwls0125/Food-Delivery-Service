package personal.yejin.foodDelivery.order.dto;

public record DiscountPreviewResponse(
        long orderId,
        int originalPrice,
        DiscountDetails discountDetails,
        int totalDiscountAmount,
        int finalPrice
) {

    public record DiscountDetails(
            int couponDiscount,
            int baeminClubDiscount
    ) {}
}
