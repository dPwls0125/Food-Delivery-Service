package personal.yejin.foodDelivery.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.foodDelivery.domain.order.model.OrderBill;
import java.util.List;

public interface OrderBillRepository extends JpaRepository<OrderBill, Long> {
    List<OrderBill> findByOrder_Id(Long orderId);
}
