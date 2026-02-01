package personal.yejin.foodDelivery.domain.order.repository;

import personal.yejin.foodDelivery.domain.order.model.AppliedDiscount;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryAppliedDiscountRepository implements AppliedDiscountRepository {

    private final Map<Long, AppliedDiscount> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public AppliedDiscount save(AppliedDiscount appliedDiscount) {
        if (appliedDiscount.getId() == null) {
            appliedDiscount.setId(sequence.incrementAndGet());
            appliedDiscount.prePersist();
        } else {
            appliedDiscount.preUpdate();
        }
        store.put(appliedDiscount.getId(), appliedDiscount);
        return appliedDiscount;
    }

    @Override
    public Optional<AppliedDiscount> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<AppliedDiscount> findByOrderBillId(Long orderBillId) {
        return store.values().stream()
                .filter(discount -> discount.getOrderBillId().equals(orderBillId))
                .collect(Collectors.toList());
    }
}
