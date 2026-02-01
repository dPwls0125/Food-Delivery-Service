package personal.yejin.foodDelivery.domain.rider.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationResponse;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat; // Using AssertJ for better assertions
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("RiderService 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    @Mock
    private RiderRepository riderRepository;

    @InjectMocks
    private RiderService riderService;

    private Rider testRider;
    private final Long TEST_RIDER_ID = 1L;
    private final double INITIAL_LAT = 37.5;
    private final double INITIAL_LON = 127.0;

    @BeforeEach
    void setUp() {
        // 테스트 라이더 초기화. Builder 패턴 사용.
        testRider = Rider.builder()
                .id(TEST_RIDER_ID)
                .name("테스트 라이더")
                .phoneNumber("010-1234-5678")
                .status(RiderStatus.READY)
                .location(new Location(INITIAL_LAT, INITIAL_LON))
                .build();
    }

    @Test
    @DisplayName("라이더 위치 업데이트 - 성공 케이스")
    void updateRiderLocation_shouldUpdateAndReturnCorrectResponse() {
        // given
        // riderRepository.findById 호출 시 testRider 반환하도록 목킹
        when(riderRepository.findById(TEST_RIDER_ID)).thenReturn(Optional.of(testRider));
        // riderRepository.save 호출 시 어떤 Rider 객체가 와도 해당 Rider 객체 반환하도록 목킹
        when(riderRepository.save(any(Rider.class))).thenReturn(testRider);

        double newLat = 37.6;
        double newLon = 127.1;

        // when
        RiderLocationResponse response = riderService.updateRiderLocation(TEST_RIDER_ID, newLat, newLon);

        // then
        // 1. riderRepository.findById가 올바른 ID로 호출되었는지 확인
        verify(riderRepository, times(1)).findById(TEST_RIDER_ID);
        // 2. rider.updateLocation이 새로운 좌표로 호출되었는지 확인 (testRider 객체가 변경되었으므로)
        assertThat(testRider.getLocation().getLatitude()).isEqualTo(newLat);
        assertThat(testRider.getLocation().getLongitude()).isEqualTo(newLon);
        // 3. riderRepository.save가 업데이트된 Rider 객체로 호출되었는지 확인
        verify(riderRepository, times(1)).save(testRider);
        // 4. 반환된 응답의 내용이 예상과 일치하는지 확인
        assertThat(response.riderId()).isEqualTo(TEST_RIDER_ID);
        assertThat(response.latitude()).isEqualTo(newLat);
        assertThat(response.longitude()).isEqualTo(newLon);
        assertThat(response.lastUpdatedAt()).isNotNull(); // 업데이트 시간은 null이 아니어야 함
    }

    @Test
    @DisplayName("라이더 위치 업데이트 - 라이더를 찾을 수 없는 경우 IllegalArgumentException 발생")
    void updateRiderLocation_shouldThrowExceptionWhenRiderNotFound() {
        // given
        // riderRepository.findById 호출 시 빈 Optional 반환하도록 목킹
        when(riderRepository.findById(anyLong())).thenReturn(Optional.empty());

        double newLat = 37.6;
        double newLon = 127.1;

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                        riderService.updateRiderLocation(999L, newLat, newLon),
                "존재하지 않는 라이더 ID로 업데이트 시 IllegalArgumentException이 발생해야 합니다.");
        // riderRepository.save는 호출되지 않아야 함
        verify(riderRepository, never()).save(any(Rider.class));
    }

    @Test
    @DisplayName("라이더 위치 조회 - 성공 케이스")
    void getRiderLocation_shouldReturnCorrectResponse() {
        // given
        when(riderRepository.findById(TEST_RIDER_ID)).thenReturn(Optional.of(testRider));

        // when
        RiderLocationResponse response = riderService.getRiderLocation(TEST_RIDER_ID);

        // then
        verify(riderRepository, times(1)).findById(TEST_RIDER_ID);
        assertThat(response.riderId()).isEqualTo(TEST_RIDER_ID);
        assertThat(response.latitude()).isEqualTo(INITIAL_LAT);
        assertThat(response.longitude()).isEqualTo(INITIAL_LON);
        assertThat(response.lastUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("라이더 위치 조회 - 라이더를 찾을 수 없는 경우 IllegalArgumentException 발생")
    void getRiderLocation_shouldThrowExceptionWhenRiderNotFound() {
        // given
        when(riderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                        riderService.getRiderLocation(999L),
                "존재하지 않는 라이더 ID로 조회 시 IllegalArgumentException이 발생해야 합니다.");
    }

    @Test
    @DisplayName("라이더 위치 조회 - 라이더는 존재하지만 위치 정보가 없는 경우 기본값 반환")
    void getRiderLocation_shouldReturnDefaultLocationWhenRiderHasNoLocation() {
        // given
        // 위치 정보가 null인 라이더 생성
        Rider riderWithNullLocation = Rider.builder()
                .id(TEST_RIDER_ID)
                .name("위치 없는 라이더")
                .phoneNumber("010-0000-0000")
                .status(RiderStatus.READY)
                .location(null) // 위치 정보를 null로 설정
                .build();
        when(riderRepository.findById(TEST_RIDER_ID)).thenReturn(Optional.of(riderWithNullLocation));

        // when
        RiderLocationResponse response = riderService.getRiderLocation(TEST_RIDER_ID);

        // then
        verify(riderRepository, times(1)).findById(TEST_RIDER_ID);
        assertThat(response.riderId()).isEqualTo(TEST_RIDER_ID);
        assertThat(response.latitude()).isEqualTo(0.0); // 기본 위도 확인
        assertThat(response.longitude()).isEqualTo(0.0); // 기본 경도 확인
        assertThat(response.lastUpdatedAt()).isNotNull();
    }
}
