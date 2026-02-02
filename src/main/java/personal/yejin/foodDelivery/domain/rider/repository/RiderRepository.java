package personal.yejin.foodDelivery.domain.rider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;

import java.util.List;

public interface RiderRepository extends JpaRepository<Rider, Long> {
    List<Rider> findByStatus(RiderStatus status);
}
