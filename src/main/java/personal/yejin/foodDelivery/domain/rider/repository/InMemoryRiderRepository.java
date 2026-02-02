package personal.yejin.foodDelivery.domain.rider.repository;

import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryRiderRepository implements RiderRepository {
    private final Map<Long, Rider> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Rider save(Rider rider) {
        if (rider.getId() == null) {
            rider.setId(sequence.incrementAndGet());
        }
        store.put(rider.getId(), rider);
        return rider;
    }

    @Override
    public Optional<Rider> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Rider> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Rider> findByStatus(RiderStatus status) {
        return store.values().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public void clear() {
        store.clear();
    }
}
