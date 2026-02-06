package personal.yejin.foodDelivery.domain.rider.service;

import java.time.LocalDateTime;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationResponse;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderRepository riderRepository;


    public RiderLocationResponse updateRiderLocation(Long riderId, double latitude, double longitude) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더 아이디가 존재하지 않습니다." + riderId));

        rider.updateLocation(latitude, longitude);
        riderRepository.save(rider);

        return new RiderLocationResponse(
                rider.getId(),
                rider.getLocation().getLatitude(),
                rider.getLocation().getLongitude(),
                LocalDateTime.now()
        );
    }

    public RiderLocationResponse getRiderLocation(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더 아이디가 존재하지 않습니다." + riderId));

        Location location = rider.getLocation();
        if (location == null) {
            return new RiderLocationResponse(
                    rider.getId(),
                    0.0,
                    0.0,
                    LocalDateTime.now()
            );
        }

        return new RiderLocationResponse(
                rider.getId(),
                location.getLatitude(),
                location.getLongitude(),
                LocalDateTime.now()
        );
    }

    public Rider assignRider(Location startLocation) {
        Rider optimalRider = riderRepository
                .findByStatus(RiderStatus.READY)
                .stream()
                .min(Comparator.comparingDouble(rider -> startLocation.calculateDistanceInHaversineFormula(rider.getLocation()))
                ).orElseThrow(() -> new IllegalStateException("배차 가능한 Rider가 존재하지 않습니다."));

        optimalRider.setStatus(RiderStatus.DISPATCHED);
        riderRepository.save(optimalRider); // TODO : JPA 변경시 필요 없음.

        return optimalRider;
    }
}
