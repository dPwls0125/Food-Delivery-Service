package personal.yejin.foodDelivery.domain.payment.dto;

public record DiscountPreviewRequest(
        long couponId,
        boolean useBaeminClub
) {}
