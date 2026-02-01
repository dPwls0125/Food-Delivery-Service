package personal.yejin.foodDelivery.domain.order.repository;

import personal.yejin.foodDelivery.domain.order.model.OrderBill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderBillRepository implements OrderBillRepository {

    private final Map<Long, OrderBill> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public OrderBill save(OrderBill orderBill) {
        if (orderBill.getId() == null) {
            orderBill.setId(sequence.incrementAndGet());
            orderBill.prePersist();
        } else {
            orderBill.preUpdate();
        }
        store.put(orderBill.getId(), orderBill);
        return orderBill;
    }

    @Override
    public Optional<OrderBill> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<OrderBill> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(bill -> bill.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }
}
