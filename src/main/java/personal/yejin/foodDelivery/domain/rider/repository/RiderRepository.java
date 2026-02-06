package personal.yejin.foodDelivery.domain.rider.repository;

import java.util.List;
import java.util.Optional;

import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;

public interface RiderRepository {
    Rider save(Rider rider);
    Optional<Rider> findById(long id);
    List<Rider> findAll();
    List<Rider> findByStatus(RiderStatus status);
}
