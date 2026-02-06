package personal.yejin.foodDelivery.domain.rider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;

public interface RiderRepository extends JpaRepository<Rider, Long> {
	List<Rider> findByStatus(RiderStatus status);
}
