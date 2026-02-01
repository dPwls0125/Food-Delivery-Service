package personal.yejin.foodDelivery.domain.order.repository;

import personal.yejin.foodDelivery.domain.order.model.AppliedDiscount;
import java.util.List;
import java.util.Optional;

public interface AppliedDiscountRepository {
    AppliedDiscount save(AppliedDiscount appliedDiscount);
    Optional<AppliedDiscount> findById(Long id);
    List<AppliedDiscount> findByOrderBillId(Long orderBillId);
}
