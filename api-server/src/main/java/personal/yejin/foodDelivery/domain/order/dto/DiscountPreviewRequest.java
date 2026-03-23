package personal.yejin.foodDelivery.domain.order.dto;

public record DiscountPreviewRequest(
        long couponId,
        boolean useBaeminClub
) {}
