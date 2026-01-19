package personal.yejin.foodDelivery.domain.route.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.rider.model.Rider;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route extends GlobalEntity {
    private Rider rider;
    private List<Stop> stops;
    private LocalDateTime estimatedArrivalTime; // 예상 도착 시간
}
