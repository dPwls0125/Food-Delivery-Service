package personal.yejin.foodDelivery.domain.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByIdIsNotAndDeliveryTypeAndStatus(Long id, DeliveryType deliveryType, DeliveryStatus status);
}
