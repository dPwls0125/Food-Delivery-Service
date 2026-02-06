package personal.yejin.foodDelivery.domain.route.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.model.StopType;
import personal.yejin.foodDelivery.domain.route.repository.RouteRepository; // 변경된 RouteRepository 임포트

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;

    public Route createSingleRoute(Delivery delivery) {
        List<Stop> stops = new ArrayList<>();
        Stop pickupStop = Stop.builder()
                .delivery(delivery)
                .type(StopType.PICKUP)
                .location(delivery.getOrder().getPickupLocation())
                .sequence(1)
                .build();
        stops.add(pickupStop);

        Stop deliveryStop = Stop.builder()
                .delivery(delivery)
                .type(StopType.DELIVERY)
                .location(delivery.getOrder().getDeliveryLocation())
                .sequence(2)
                .build();
        stops.add(deliveryStop);

        return routeRepository.save(Route.createRouteWithoutRider(stops));
    }

    // 최적의 Route 할당
    public Route getOptimalRouteWithoutRider(Delivery delivery1, Delivery delivery2) {

        List<Stop> rawStops = createRawStops(delivery1, delivery2);

        // 모든 유효한 Stop 시퀀스 생성 및 최적 경로 찾기
        List<List<Stop>> validSequences = generateAllValidStopSequences(rawStops, delivery1, delivery2);

        List<Stop> optimalSequence = validSequences.stream()
                .min(Comparator.comparingDouble(this::calculateSequenceTotalDistance))
                .orElseThrow(() -> new IllegalStateException("최적의 시퀀스를 찾을 수 없습니다."));

        // 시퀀스 설정 (sequence 필드 업데이트)
        for (int i = 0; i < optimalSequence.size(); i++) {
            optimalSequence.get(i).setSequence(i + 1);
        }

        return routeRepository.save(Route.createRouteWithoutRider(optimalSequence));
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
            totalDistance += stops.get(i).getLocation().calculateDistanceInHaversineFormula(stops.get(i + 1).getLocation());
        }
        return totalDistance;
    }
}
