package personal.yejin.foodDelivery.payment.dto;

public record DiscountPreviewRequest(
        long couponId,
        boolean useBaeminClub
) {}
