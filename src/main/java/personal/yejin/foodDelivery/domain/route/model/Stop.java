package personal.yejin.foodDelivery.domain.route.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.rider.model.Location;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stop extends GlobalEntity {
    private Route route;
    private Delivery delivery;
    private StopType type;
    private Location location;
    private LocalDateTime estimatedTime; // 예상 시간
    private LocalDateTime completedTime; // 완료 시간
    private int sequence;

    public void assignRoute(Route route) {
        this.route = route;
    }

    public boolean isCompleted() {
        return completedTime != null;
    }

    public void complete() {
        this.completedTime = LocalDateTime.now();
    }

    public void setSequence(int sequence) { // Changed 'i' to 'sequence' for clarity
        this.sequence = sequence;
    }


}
