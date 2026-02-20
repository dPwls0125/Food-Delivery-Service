package personal.yejin.foodDelivery.domain.rider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationResponse;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;

import java.time.LocalDateTime;
import java.util.Comparator;

@Slf4j
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
                rider.getUpdatedAt()
        );
    }

    public RiderLocationResponse getRiderLocation(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더 아이디가 존재하지 않습니다." + riderId));

        Location location = rider.getLocation();
        LocalDateTime lastUpdatedAt = rider.getUpdatedAt();

        if (location == null) {
            return new RiderLocationResponse(
                    rider.getId(),
                    0.0,
                    0.0,
                    lastUpdatedAt
            );
        }

        return new RiderLocationResponse(
                rider.getId(),
                location.getLatitude(),
                location.getLongitude(),
                lastUpdatedAt
        );
    }

    public Rider assignRider(Location startLocation) {
        Rider optimalRider = riderRepository
                .findByStatus(RiderStatus.READY)
                .stream()
                .filter(rider -> rider.getLocation() != null)
                .min(Comparator.comparingDouble(rider -> {
                    return startLocation.calculateDistanceInHaversineFormula(rider.getLocation());
                }))
                .orElseThrow(() -> new IllegalStateException("배차 가능한 Rider가 존재하지 않습니다."));

        optimalRider.setStatus(RiderStatus.DISPATCHED);
        riderRepository.save(optimalRider); // 상태 변경 후 저장
        return optimalRider;
    }

    // DB 쿼리와 인덱스(Bounding Box)를 활용하여 최적화된 라이더를 찾는 메서드
    public Rider assignRiderOptimized(Location startLocation) {
        double range = 0.05; // 약 5km 범위
        double minLat = startLocation.getLatitude() - range;
        double maxLat = startLocation.getLatitude() + range;
        double minLon = startLocation.getLongitude() - range;
        double maxLon = startLocation.getLongitude() + range;

        Rider optimalRider = riderRepository.findNearestRiderByStatusWithBounds(
                        RiderStatus.READY.name(),
                        startLocation.getLatitude(),
                        startLocation.getLongitude(),
                        minLat, maxLat, minLon, maxLon)
                .orElseThrow(() -> new IllegalStateException("주변 5km 이내에 배차 가능한 Rider가 존재하지 않습니다."));

        optimalRider.setStatus(RiderStatus.DISPATCHED);
        riderRepository.save(optimalRider);
        return optimalRider;
    }
}
