package personal.yejin.foodDelivery.domain.delivery.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.service.RouteService;


@RequiredArgsConstructor
@Service
@Transactional(readOnly = true) // 클래스 레벨에 readOnly 트랜잭션 적용
public class DeliveryService {
    private static final double BUNDLE_RADIUS_KM = 2.0;

    private final RiderService riderService;
    private final RouteService routeService;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public Optional<Route> createSingleDelivery(Delivery delivery) {
        // 1. 단일 배송을 위한 Route 생성
        Route route = routeService.createSingleRoute(delivery);

        // 2. 최적의 라이더 배정
        Stop startPoint = route.getStartLocation();
        Rider riderOptimal = riderService.assignRider(startPoint.getLocation());

        // 3. Route에 라이더 할당 및 정보 업데이트
        route.assignRider(riderOptimal);

        delivery.dispatch(route);

        return Optional.of(route);
    }

    @Transactional
    public Optional<Route> attemptToBundle(Delivery delivery1) {
        if (delivery1.getDeliveryType() != DeliveryType.BUNDLE || delivery1.getStatus() != DeliveryStatus.PENDING) {
            return Optional.empty();
        }

        Optional<Delivery> candidateOpt = findBundleCandidate(delivery1);
        if (candidateOpt.isEmpty()) {
            return Optional.empty(); // TODO : 예외 처리 or 단일 배송으로 처리
        }
        Delivery delivery2 = candidateOpt.get();

        Route route = routeService.getOptimalRouteWithoutRider(delivery1, delivery2);
        // 최적 경로의 시작 위치를 가져와 가장 가까운 최적의 라이더를 찾습니다.
        Stop startPoint = route.getStartLocation();
        Rider riderOptimal = riderService.assignRider(startPoint.getLocation());

        // 전체 Route 생성
        route.assignRider(riderOptimal);

        delivery1.dispatch(route);
        delivery2.dispatch(route);

        return Optional.of(route);
    }


    private Optional<Delivery> findBundleCandidate(Delivery delivery) {
        Location pickupLocation1 = delivery.getOrder().getPickupLocation();

        List<Delivery> candidates = deliveryRepository.findByIdIsNotAndDeliveryTypeAndStatus(
                delivery.getId(),
                DeliveryType.BUNDLE,
                DeliveryStatus.PENDING
        );

        return candidates.stream()
                .filter(candidate -> {
                    Location pickupLocation2 = candidate.getOrder().getPickupLocation();
                    double distance = pickupLocation1.calculateDistanceInHaversineFormula(pickupLocation2);
                    return distance <= BUNDLE_RADIUS_KM;
                })
                .min(Comparator.comparingDouble(c -> pickupLocation1.calculateDistanceInHaversineFormula(c.getOrder().getPickupLocation())));
    }


}
