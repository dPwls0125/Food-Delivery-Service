package personal.yejin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.model.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
