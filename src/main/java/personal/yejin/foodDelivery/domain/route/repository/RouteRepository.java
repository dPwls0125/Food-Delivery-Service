package personal.yejin.foodDelivery.domain.route.repository;

import java.util.Optional;

import personal.yejin.foodDelivery.domain.route.model.Route;

public interface RouteRepository {
    Route save(Route route);
    Optional<Route> findById(long id);
}
