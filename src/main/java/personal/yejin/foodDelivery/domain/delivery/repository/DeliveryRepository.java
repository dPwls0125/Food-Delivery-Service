package personal.yejin.foodDelivery.domain.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType; // import 추가
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus; // import 추가

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByIdIsNotAndDeliveryTypeAndStatus(Long id, DeliveryType deliveryType, DeliveryStatus status);
}
