package personal.yejin.foodDelivery.domain.delivery.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.model.StopType;
import personal.yejin.foodDelivery.domain.route.repository.RouteRepository;

public class DispatchService {
    private final DeliveryRepository deliveryRepository;
    private final RouteRepository routeRepository;
    private final RiderRepository riderRepository;

    private static final double BUNDLE_RADIUS_KM = 2.0; // 2km 이내

    public DispatchService(DeliveryRepository deliveryRepository, RouteRepository routeRepository, RiderRepository riderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.routeRepository = routeRepository;
        this.riderRepository = riderRepository;
    }

    public Optional<Route> attemptToBundle(Delivery delivery1) {
        if (delivery1.getDeliveryType() != DeliveryType.BUNDLE || delivery1.getStatus() != DeliveryStatus.PENDING) {
            return Optional.empty();
        }

        Optional<Delivery> candidateOpt = findBundleCandidate(delivery1);
        if (candidateOpt.isEmpty()) {
            return Optional.empty(); // TODO : 예외 처리 or 단일 배송으로 처리
        }
        Delivery delivery2 = candidateOpt.get();

        // 4개의 Stop 객체 생성
        List<Stop> rawStops = createRawStops(delivery1, delivery2);

        // 모든 유효한 Stop 시퀀스 생성 및 최적 경로 찾기
        List<List<Stop>> validSequences = generateAllValidStopSequences(rawStops, delivery1, delivery2);

        if (validSequences.isEmpty()) {
            return Optional.empty(); // TODO : 예외 처리 or 단일 배송으로 처리
        }

        List<Stop> optimalSequence = validSequences.stream()
                .min(Comparator.comparingDouble(this::calculateSequenceTotalDistance))
                .orElseThrow(() -> new IllegalStateException("최적의 시퀀스를 찾을 수 없습니다."));
        
        // 시퀀스 설정 (sequence 필드 업데이트)
        for (int i = 0; i < optimalSequence.size(); i++) {
            optimalSequence.get(i).setSequence(i + 1);
        }

        // 최적 경로의 시작 위치를 가져와 가장 가까운 라이더를 찾습니다.
        Location routeStartLocation = optimalSequence.get(0).getLocation();
        Optional<Rider> riderOpt = findAvailableRider(routeStartLocation);
        if (riderOpt.isEmpty()) {
            return Optional.empty();
        }
        Rider rider = riderOpt.get();

        // 전체 Route 생성
        Route route = Route.builder().rider(rider).stops(optimalSequence).build();
        routeRepository.save(route);

        // 각 Stop에 Route 설정
        optimalSequence.forEach(stop -> stop.setRoute(route));

        // Update deliveries and rider
        delivery1.dispatch(route);
        delivery2.dispatch(route);
        deliveryRepository.save(delivery1);
        deliveryRepository.save(delivery2);

        rider.setStatus(RiderStatus.DISPATCHED);
        riderRepository.save(rider);

        return Optional.of(route);
    }

    private Optional<Rider> findAvailableRider(Location routeStartLocation) {
        List<Rider> readyRiders = riderRepository.findByStatus(RiderStatus.READY);
        if (readyRiders.isEmpty()) {
            return Optional.empty();
        }

        return readyRiders.stream()
                .min(Comparator.comparingDouble(rider -> calculateDistance(rider.getLocation(), routeStartLocation)));
    }

    private Optional<Delivery> findBundleCandidate(Delivery delivery) {
        Location pickupLocation1 = delivery.getOrder().getPickupLocation();

        List<Delivery> candidates = deliveryRepository.findAll().stream()
                .filter(d -> !d.getId().equals(delivery.getId()) &&
                             d.getDeliveryType() == DeliveryType.BUNDLE &&
                             d.getStatus() == DeliveryStatus.PENDING)
                .collect(Collectors.toList());

        return candidates.stream()
                .filter(candidate -> {
                    Location pickupLocation2 = candidate.getOrder().getPickupLocation();
                    double distance = calculateDistance(pickupLocation1, pickupLocation2);
                    return distance <= BUNDLE_RADIUS_KM;
                })
                .min(Comparator.comparingDouble(c -> calculateDistance(pickupLocation1, c.getOrder().getPickupLocation())));
    }

    /**
     * Haversine formula to calculate distance between two lat/lon points
     * @return distance in kilometers
     * 지구 곡률을 반영하여 계산하는 공식
     */
    private double calculateDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return Double.MAX_VALUE;
        }
        
        double lat1 = loc1.getLatitude();
        double lon1 = loc1.getLongitude();
        double lat2 = loc2.getLatitude();
        double lon2 = loc2.getLongitude();

        double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 4개의 Stop 객체 생성
    private List<Stop> createRawStops(Delivery d1, Delivery d2) {
        List<Stop> rawStops = new ArrayList<>();
        rawStops.add(Stop.builder().delivery(d1).type(StopType.PICKUP).location(d1.getOrder().getPickupLocation()).build());
        rawStops.add(Stop.builder().delivery(d1).type(StopType.DELIVERY).location(d1.getOrder().getDeliveryLocation()).build());
        rawStops.add(Stop.builder().delivery(d2).type(StopType.PICKUP).location(d2.getOrder().getPickupLocation()).build());
        rawStops.add(Stop.builder().delivery(d2).type(StopType.DELIVERY).location(d2.getOrder().getDeliveryLocation()).build());
        return rawStops;
    }

    // 가능한 Stop 조합에 대한 모든 순열 생성
    private List<List<Stop>> generateAllValidStopSequences(List<Stop> rawStops, Delivery d1, Delivery d2) {
        List<List<Stop>> validSequences = new ArrayList<>();
        List<Stop> currentPermutation = new ArrayList<>();
        boolean[] used = new boolean[rawStops.size()];

        permute(rawStops, used, currentPermutation, validSequences, d1, d2);
        return validSequences;
    }

    private void permute(List<Stop> rawStops, boolean[] used, List<Stop> currentPermutation,
                         List<List<Stop>> validSequences, Delivery d1, Delivery d2) {
        if (currentPermutation.size() == rawStops.size()) {
            if (isValidSequence(currentPermutation, d1, d2)) {
                validSequences.add(new ArrayList<>(currentPermutation));
            }
            return;
        }

        for (int i = 0; i < rawStops.size(); i++) {
            if (!used[i]) {
                used[i] = true;
                currentPermutation.add(rawStops.get(i));
                permute(rawStops, used, currentPermutation, validSequences, d1, d2);
                currentPermutation.remove(currentPermutation.size() - 1);
                used[i] = false;
            }
        }
    }

    // 유효한 Stop 시퀀스인지 확인
    private boolean isValidSequence(List<Stop> sequence, Delivery d1, Delivery d2) {
        int p1Index = -1, d1Index = -1;
        int p2Index = -1, d2Index = -1;

        for (int i = 0; i < sequence.size(); i++) {
            Stop stop = sequence.get(i);
            if (stop.getDelivery().getId().equals(d1.getId())) {
                if (stop.getType() == StopType.PICKUP) p1Index = i;
                else d1Index = i;
            } else if (stop.getDelivery().getId().equals(d2.getId())) {
                if (stop.getType() == StopType.PICKUP) p2Index = i;
                else d2Index = i;
            }
        }
        // 픽업이 배송보다 먼저 오는지 확인
        if (p1Index == -1 || d1Index == -1 || p2Index == -1 || d2Index == -1) {
            return false;
        }

        return p1Index < d1Index && p2Index < d2Index;
    }

    // 시퀀스의 총 거리 계산
    private double calculateSequenceTotalDistance(List<Stop> stops) {
        double totalDistance = 0;
        if (stops.isEmpty()) {
            return totalDistance;
        }
        
        for (int i = 0; i < stops.size() - 1; i++) {
            totalDistance += calculateDistance(stops.get(i).getLocation(), stops.get(i + 1).getLocation());
        }
        return totalDistance;
    }
}
