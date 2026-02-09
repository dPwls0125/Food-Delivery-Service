package personal.yejin.foodDelivery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

@SpringBootTest
@Transactional
public class PerformanceTest {

    @Autowired
    private RiderService riderService;

    @Autowired
    private RiderRepository riderRepository;

    private static final int NUMBER_OF_RIDERS = 100; // 성능 측정을 위한 라이더 수

    @BeforeEach
    void setUp() {
        riderRepository.deleteAll(); // 기존 데이터 삭제

        // 대량의 Ready 상태 라이더를 DB에 저장
        List<Rider> ridersToSave = new ArrayList<>();
        IntStream.range(0, NUMBER_OF_RIDERS).forEach(i -> {
            ridersToSave.add(Rider.builder()
                    .name("Test Rider " + i)
                    .location(new Location(37.5 + (i * 0.00001), 127.0 + (i * 0.00001))) // 약간씩 다른 위치
                    .status(RiderStatus.READY)
                    .build());
        });
        riderRepository.saveAll(ridersToSave);
    }

    @Test
    @DisplayName("assignRider 메서드 성능 측정 (서버에서 필터링)")
    void testAssignRiderPerformance() {
        Location startLocation = new Location(37.5, 127.0);
        riderService.assignRider(startLocation);
        // AOP 로그로 성능 측정 결과 확인
    }

    @Test
    @DisplayName("assignRiderOptimized 메서드 성능 측정 (DB 쿼리 최적화)")
    void testAssignRiderOptimizedPerformance() {
        Location startLocation = new Location(37.5, 127.0);
        riderService.assignRiderOptimized(startLocation);
        // AOP 로그로 성능 측정 결과 확인
    }
}
