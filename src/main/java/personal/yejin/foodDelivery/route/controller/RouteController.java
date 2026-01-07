package personal.yejin.foodDelivery.route.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal.yejin.foodDelivery.rider.dto.CurrentRouteResponse;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    @GetMapping("/me/current-route")
    public ResponseEntity<CurrentRouteResponse> getCurrentRoute() {
        List<CurrentRouteResponse.Stop> fakeStops = List.of(
                new CurrentRouteResponse.Stop(1, CurrentRouteResponse.StopType.PICKUP, 5001L, "가게 A"),
                new CurrentRouteResponse.Stop(2, CurrentRouteResponse.StopType.PICKUP, 5002L, "가게 B"),
                new CurrentRouteResponse.Stop(3, CurrentRouteResponse.StopType.DELIVERY, 5001L, "고객 A"),
                new CurrentRouteResponse.Stop(4, CurrentRouteResponse.StopType.DELIVERY, 5002L, "고객 B")
        );
        CurrentRouteResponse fakeResponse = new CurrentRouteResponse(fakeStops);
        return ResponseEntity.ok(fakeResponse);
    }
}
