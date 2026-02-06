package personal.yejin.foodDelivery.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.foodDelivery.domain.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
