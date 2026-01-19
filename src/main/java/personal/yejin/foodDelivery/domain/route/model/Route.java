package personal.yejin.foodDelivery.domain.route.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.rider.model.Rider;

@Getter
public class Route extends GlobalEntity {
    private Rider rider;
    private List<Stop> stops;
    private LocalDateTime estimatedArrivalTime; // 예상 도착 시간

    @Builder
    public Route(Rider rider, List<Stop> stops) {
        this.rider = rider;
        this.stops = stops;
    }
}
