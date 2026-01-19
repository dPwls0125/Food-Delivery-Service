package personal.yejin.foodDelivery.domain.rider.repository;

import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;

import java.util.List;
import java.util.Optional;

public interface RiderRepository {
    Rider save(Rider rider);
    Optional<Rider> findById(long id);
    List<Rider> findAll();
    List<Rider> findByStatus(RiderStatus status);
}
