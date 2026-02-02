package personal.yejin.foodDelivery.domain.route.repository;

import org.springframework.stereotype.Repository;
import personal.yejin.foodDelivery.domain.route.model.Route;

import java.util.Optional;

@Repository
public interface RouteRepository {
    Route save(Route route);
    Optional<Route> findById(long id);
}
