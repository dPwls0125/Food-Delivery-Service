package personal.yejin.foodDelivery.domain.delivery.repository;

import personal.yejin.foodDelivery.domain.delivery.model.Delivery;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(long id);
    List<Delivery> findAll();
}
