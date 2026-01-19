package personal.yejin.foodDelivery.domain.route.repository;

import personal.yejin.foodDelivery.domain.route.model.Route;

import java.util.Optional;

public interface RouteRepository {
    Route save(Route route);
    Optional<Route> findById(long id);
}
