package personal.yejin.foodDelivery.domain.rider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        notifyNearArrivalIfNeededAsync(rider);
        return new RiderLocationResponse(
                rider.getId(),
                rider.getLocation().getLatitude(),
                rider.getLocation().getLongitude(),
                rider.getUpdatedAt()
        );
    }

    private void notifyNearArrivalIfNeededAsync(Rider rider) {
        List<Delivery> activeDeliveries = getActiveDeliveries(rider.getId());

        for (Delivery delivery : activeDeliveries) {
            if (delivery.isNearArrivalNotified()) {
                continue;
            }
            Location destination = delivery.getOrder().getDeliveryLocation();
            double distanceKm = rider.getLocation().calculateDistanceInHaversineFormula(destination);

            if (distanceKm <= NEAR_ARRIVAL_THRESHOLD_KM) {
                delivery.markNearArrivalNotified();
                CompletableFuture.runAsync(() -> {
                    try {
                        orderDeliveryNotificationService.notifyNearArrival(delivery, distanceKm);
                    } catch (Exception e) {
                        log.error("배달 근점 알림 비동기 전송 중 에러 발생: deliveryId={}", delivery.getId(), e);
                    }
                });
            }
        }
    }

    @Cacheable(value = "activateDeliveries", key = "#riderId")
    public List<Delivery> getActiveDeliveries(Long riderId) {
        return deliveryRepository.findByRiderIdAndStatusIn(
                riderId,
                List.of(DeliveryStatus.DISPATCHED, DeliveryStatus.PICKED_UP)
        );
    }

    private void notifyNearArrivalIfNeeded(Rider rider) {

        List<Delivery> activeDeliveries = getActiveDeliveries(rider.getId());

        for (Delivery delivery : activeDeliveries) {
            if (delivery.isNearArrivalNotified()) {
                continue;
            }
            Location destination = delivery.getOrder().getDeliveryLocation();
            double distanceKm = rider.getLocation().calculateDistanceInHaversineFormula(destination);

            if (distanceKm <= NEAR_ARRIVAL_THRESHOLD_KM) {
                delivery.markNearArrivalNotified();
                orderDeliveryNotificationService.notifyNearArrival(delivery, distanceKm);
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
