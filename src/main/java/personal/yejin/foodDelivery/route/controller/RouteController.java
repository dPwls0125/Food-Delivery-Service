package personal.yejin.foodDelivery.route.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal.yejin.foodDelivery.route.dto.CurrentRouteResponse;
import personal.yejin.foodDelivery.route.dto.Stop;
import personal.yejin.foodDelivery.route.model.StopType;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {
    @GetMapping("/me/current-route")
    public ResponseEntity<CurrentRouteResponse> getCurrentRoute() {
        List<Stop> fakeStops = List.of(
                new Stop(1, StopType.PICKUP, 5001L, "가게 A"),
                new Stop(2, StopType.PICKUP, 5002L, "가게 B"),
                new Stop(3, StopType.DELIVERY, 5001L, "고객 A"),
                new Stop(4, StopType.DELIVERY, 5002L, "고객 B")
        );
        CurrentRouteResponse fakeResponse = new CurrentRouteResponse(fakeStops);
        return ResponseEntity.ok(fakeResponse);
    }
}
