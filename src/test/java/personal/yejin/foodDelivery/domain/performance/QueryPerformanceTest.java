package personal.yejin.foodDelivery.domain.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@Transactional
public class QueryPerformanceTest {

    @Autowired
    private RiderService riderService;

    @Autowired
    private RiderRepository riderRepository;

    private static final int NUMBER_OF_RIDERS = 5000; // 성능 측정을 위한 라이더 수
    private static final int NUMBER_OF_RUNS = 100; // 각 메서드를 실행할 횟수

    @BeforeEach
    void setUp() {
        riderRepository.deleteAll(); // 기존 데이터 삭제
        List<Rider> ridersToSave = new ArrayList<>();
        IntStream.range(0, NUMBER_OF_RIDERS).forEach(i -> {
            RiderStatus status = RiderStatus.READY;
            if (i > 2500) {
                status = RiderStatus.DISPATCHED;
            }
            ridersToSave.add(Rider.builder()
                    .name("TestLogging Rider " + i)
                    .location(new Location(37.5 + (i * 0.00001), 127.0 + (i * 0.00001)))
                    .status(status)
                    .build());
        });
        riderRepository.saveAll(ridersToSave);
    }

    @Test
    @DisplayName("assignRider 메서드 성능 측정 (서버에서 필터링)")
    void testAssignRiderPerformance() {
        Location startLocation = new Location(37.5, 127.0);
        List<Long> durations = new ArrayList<>();
        System.out.println("\n--- assignRider Performance ---");
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            long startTime = System.nanoTime();
            riderService.assignRider(startLocation);
            long endTime = System.nanoTime();
            durations.add((endTime - startTime) / 1_000_000); // 밀리초 단위
        }
        double averageDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.printf("Average execution time: %.2f ms\n", averageDuration);
        System.out.println("---------------------------------");
    }

    @Test
    @DisplayName("assignRiderOptimized 메서드 성능 측정 (DB 쿼리 최적화)")
    void testAssignRiderOptimizedPerformance() {
        Location startLocation = new Location(37.5, 127.0);
        List<Long> durations = new ArrayList<>();
        System.out.println("\n--- assignRiderOptimized Performance ---");
        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            long startTime = System.nanoTime();
            riderService.assignRiderOptimized(startLocation);
            long endTime = System.nanoTime();
            durations.add((endTime - startTime) / 1_000_000); // 밀리초 단위
        }
        double averageDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.printf("Average execution time: %.2f ms\n", averageDuration);
        System.out.println("----------------------------------------");
    }
}
