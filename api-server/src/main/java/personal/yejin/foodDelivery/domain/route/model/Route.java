package personal.yejin.foodDelivery.domain.route.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.rider.model.Rider;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Route extends GlobalEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Stop> stops = new ArrayList<>();

    public static Route createRouteWithoutRider(List<Stop> stops) {
        Route route = Route.builder().build();
        for (Stop stop : stops) {
            route.addStop(stop);
        }
        return route;
    }

    public void addStop(Stop stop) {
        this.stops.add(stop);
        stop.assignRoute(this);
    }

    public void assignRider(Rider rider) {
        this.rider = rider;
    }

    public Stop getStartLocation() {
        if(stops.isEmpty()){
            throw new IllegalStateException("Route의 경로가 지정되지 않았습니다.");
        }
        return stops.get(0);
    }

}
