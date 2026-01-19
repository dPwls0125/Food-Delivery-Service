package personal.yejin.foodDelivery.rider.dto;

import java.time.LocalDateTime;

public record RiderLocationResponse(
    long riderId,
    double latitude,
    double longitude,
    LocalDateTime lastUpdatedAt
) {
}
