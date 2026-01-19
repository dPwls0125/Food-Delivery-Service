package personal.yejin.foodDelivery.domain.order.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import personal.yejin.foodDelivery.domain.order.model.Order;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(sequence.incrementAndGet());
            order.prePersist(); // Simulate @PrePersist for new entities
        } else {
            order.preUpdate(); // Simulate @PreUpdate for existing entities
        }
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
