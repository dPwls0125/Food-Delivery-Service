package personal.yejin.foodDelivery.domain.rider.service;

import java.time.LocalDateTime;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationResponse;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderRepository riderRepository;

    public RiderLocationResponse updateRiderLocation(Long riderId, double latitude, double longitude) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더 아이디가 존재하지 않습니다." + riderId));

        rider.updateLocation(latitude, longitude);
        riderRepository.save(rider); // Uncommented for mock verification

        return new RiderLocationResponse(
                rider.getId(),
                rider.getLocation().getLatitude(),
                rider.getLocation().getLongitude(),
                rider.getUpdatedAt() // Use updated time from rider entity
        );
    }

    public RiderLocationResponse getRiderLocation(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더 아이디가 존재하지 않습니다." + riderId));

        Location location = rider.getLocation();
        LocalDateTime lastUpdatedAt = rider.getUpdatedAt(); // Get updated time from rider entity

        if (location == null) {
            return new RiderLocationResponse(
                    rider.getId(),
                    0.0,
                    0.0,
                    lastUpdatedAt // Use updated time from rider entity
            );
        }

        return new RiderLocationResponse(
                rider.getId(),
                location.getLatitude(),
                location.getLongitude(),
                lastUpdatedAt // Use updated time from rider entity
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

    // DB 쿼리로 최적화된 라이더를 찾는 메서드를 Java 로직으로 변경
    public Rider assignRiderOptimized(Location startLocation) {
        log.info("Searching for nearest rider with startLatitude: {}, startLongitude: {}", startLocation.getLatitude(), startLocation.getLongitude());

        // 1. 모든 READY 상태의 라이더를 조회
        Rider optimalRider = riderRepository.findNearestRiderByStatus(RiderStatus.READY.name(), startLocation.getLatitude(),
                startLocation.getLongitude())
                .orElseThrow(() -> new IllegalStateException("배차 가능한 Rider가 존재하지 않습니다.")); // 최적 라이더가 없으면 예외 발생

        // 2. 최적 라이더의 상태 변경 및 저장
        optimalRider.setStatus(RiderStatus.DISPATCHED);
        riderRepository.save(optimalRider);
        return optimalRider;
    }
}
