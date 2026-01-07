package personal.yejin.foodDelivery.order.dto;

public record DiscountPreviewRequest(
        long couponId,
        boolean useBaeminClub
) {}
