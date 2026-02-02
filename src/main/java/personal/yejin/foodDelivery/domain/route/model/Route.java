package personal.yejin.foodDelivery.domain.route.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.rider.model.Rider;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route extends GlobalEntity {

    private Rider rider;
    private List<Stop> stops;

    public static Route createRouteWithoutRider(List<Stop> stops) {
        Route route = new Route();
        route.stops = new ArrayList<>();
        for (Stop stop : stops) {
            route.stops.add(stop);
            stop.assignRoute(route);
        }

        return route;
    }

    public void assignRider(Rider rider) {
        this.rider = rider;
    }

    public Stop getStartLocation() {
        return stops.get(0);
    }

}
