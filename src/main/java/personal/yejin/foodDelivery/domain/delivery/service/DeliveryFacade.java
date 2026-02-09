package personal.yejin.foodDelivery.domain.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.service.RouteService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeliveryFacade {
    private final RiderService riderService;
    private final RouteService routeService;
    private final DeliveryService deliveryService;

    @Transactional
    public Optional<Route> createSingleDelivery(Delivery delivery) {
        Route route = routeService.createSingleRoute(delivery);
        Stop startPoint = route.getStartLocation();
        Rider riderOptimal = riderService.assignRider(startPoint.getLocation());
        return Optional.of(deliveryService.dispatchSingle(route, riderOptimal, delivery));
    }

    @Transactional
    public Optional<Route> attemptToBundle(Delivery delivery1) {
        if (!deliveryService.isBundleEligible(delivery1)) {
            return Optional.empty();
        }

        Optional<Delivery> candidateOpt = deliveryService.findBundleCandidate(delivery1);
        if (candidateOpt.isEmpty()) {
            return Optional.empty();
        }

        Delivery delivery2 = candidateOpt.get();
        Route route = routeService.getOptimalRouteWithoutRider(delivery1, delivery2);
        Stop startPoint = route.getStartLocation();
        Rider riderOptimal = riderService.assignRider(startPoint.getLocation());

        return Optional.of(deliveryService.dispatchBundle(route, riderOptimal, delivery1, delivery2));
    }
}
