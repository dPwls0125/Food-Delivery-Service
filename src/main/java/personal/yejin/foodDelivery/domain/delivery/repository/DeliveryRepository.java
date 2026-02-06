package personal.yejin.foodDelivery.domain.delivery.repository;

import java.util.List;
import java.util.Optional;

import personal.yejin.foodDelivery.domain.delivery.model.Delivery;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(long id);
    List<Delivery> findAll();
}
