package personal.yejin.foodDelivery.domain.delivery.repository;

import org.springframework.stereotype.Repository;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(long id);
    List<Delivery> findAll();
}
