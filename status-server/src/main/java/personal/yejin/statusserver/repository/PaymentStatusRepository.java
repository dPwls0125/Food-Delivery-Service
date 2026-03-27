package personal.yejin.statusserver.repository;

import org.springframework.data.repository.CrudRepository;
import personal.yejin.statusserver.domain.PaymentStatusEntity;

public interface PaymentStatusRepository extends CrudRepository<PaymentStatusEntity, Long> {
}
