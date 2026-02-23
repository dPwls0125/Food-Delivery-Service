package personal.yejin.foodDelivery.domain.rider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.order.service.OrderDeliveryNotificationService;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationResponse;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderService {
    private static final double NEAR_ARRIVAL_THRESHOLD_KM = 0.5;

    private final RiderRepository riderRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderDeliveryNotificationService orderDeliveryNotificationService;

    @Transactional
    public RiderLocationResponse updateRiderLocation(Long riderId, double latitude, double longitude) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("라이더 아이디가 존재하지 않습니다." + riderId));

        rider.updateLocation(latitude, longitude);
        notifyNearArrivalIfNeeded(rider);

        return new RiderLocationResponse(
                rider.getId(),
                rider.getLocation().getLatitude(),
                rider.getLocation().getLongitude(),
                rider.getUpdatedAt()
        );
    }

    private void notifyNearArrivalIfNeeded(Rider rider) {
        List<Delivery> activeDeliveries = deliveryRepository.findByRiderIdAndStatusIn(
                rider.getId(),
                List.of(DeliveryStatus.DISPATCHED, DeliveryStatus.PICKED_UP)
        );

        for (Delivery delivery : activeDeliveries) {
            if (delivery.isNearArrivalNotified()) {
                continue;
            }

            Location destination = delivery.getOrder().getDeliveryLocation();
            double distanceKm = rider.getLocation().calculateDistanceInHaversineFormula(destination);

            if (distanceKm <= NEAR_ARRIVAL_THRESHOLD_KM) {
                orderDeliveryNotificationService.notifyNearArrival(delivery, distanceKm);
                delivery.markNearArrivalNotified();
            }
        }
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

    @Transactional
    @Deprecated
    public Rider assignRider(Location startLocation) {
        Rider optimalRider = riderRepository
                .findByStatusWithLock(RiderStatus.READY)
                .stream()
                .filter(rider -> rider.getLocation() != null)
                .min(Comparator.comparingDouble(rider -> {
                    return startLocation.calculateDistanceInHaversineFormula(rider.getLocation());
                }))
                .orElseThrow(() -> new IllegalStateException("배차 가능한 Rider가 존재하지 않습니다."));

        optimalRider.setStatus(RiderStatus.DISPATCHED);
        return optimalRider;
    }


    @Transactional
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
        return optimalRider;
    }
}
