package personal.yejin.foodDelivery.domain.delivery.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import personal.yejin.foodDelivery.domain.delivery.model.Delivery;

@Repository
public class InMemoryDeliveryRepository implements DeliveryRepository {
    private final Map<Long, Delivery> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Delivery save(Delivery delivery) {
        if (delivery.getId() == null) {
            delivery.setId(sequence.incrementAndGet());
        }
        store.put(delivery.getId(), delivery);
        return delivery;
    }

    @Override
    public Optional<Delivery> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Delivery> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clear() {
        store.clear();
    }
}
