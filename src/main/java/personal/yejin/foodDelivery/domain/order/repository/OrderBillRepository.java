package personal.yejin.foodDelivery.domain.order.repository;

import personal.yejin.foodDelivery.domain.order.model.OrderBill;
import java.util.Optional;
import java.util.List;

public interface OrderBillRepository {
    OrderBill save(OrderBill orderBill);
    Optional<OrderBill> findById(Long id);
    List<OrderBill> findByOrderId(Long orderId);
}
