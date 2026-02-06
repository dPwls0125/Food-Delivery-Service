package personal.yejin.foodDelivery.domain.route.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import personal.yejin.foodDelivery.domain.route.model.Route;

@Repository
public class InMemoryRouteRepository implements RouteRepository {
    private final Map<Long, Route> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Route save(Route route) {
        if (route.getId() == null) {
            route.setId(sequence.incrementAndGet());
        }
        store.put(route.getId(), route);
        return route;
    }

    @Override
    public Optional<Route> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void clear() {
        store.clear();
    }
}
