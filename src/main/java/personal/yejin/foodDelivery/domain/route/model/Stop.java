package personal.yejin.foodDelivery.domain.route.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.rider.model.Location;

@Getter
public class Stop extends GlobalEntity {
    private Route route;
    private Delivery delivery;
    private StopType type;
    private Location location;
    private LocalDateTime estimatedTime; // 예상 시간
    private LocalDateTime completedTime; // 완료 시간
    private int sequence;

    @Builder
    public Stop(Delivery delivery, StopType type, Location location, int sequence) {
        this.delivery = delivery;
        this.type = type;
        this.location = location;
        this.sequence = sequence;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public boolean isCompleted() {
        return completedTime != null;
    }

    public void complete() {
        this.completedTime = LocalDateTime.now();
    }

    public void setSequence(int i) {
        sequence = i;
    }
}
