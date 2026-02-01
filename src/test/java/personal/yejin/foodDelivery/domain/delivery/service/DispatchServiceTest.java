package personal.yejin.foodDelivery.domain.delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.model.StopType;
import personal.yejin.foodDelivery.domain.route.repository.RouteRepository;

@DisplayName("DispatchService 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

	@Mock
	private DeliveryRepository deliveryRepository;
	@Mock
	private RouteRepository routeRepository;
	@Mock
	private RiderRepository riderRepository;

	@InjectMocks
	private DispatchService dispatchService;

	// 테스트 데이터 생성을 위한 헬퍼 메서드
	private Delivery createTestDelivery(Long id, DeliveryType type, DeliveryStatus status, Location pickup,
		Location delivery) {
		Order order = Order.builder()
			.storeId(100L)
			.deliveryAddress("Test Address")
			.deliveryType(type)
			.pickupLocation(pickup)
			.deliveryLocation(delivery)
			.orderItems(Collections.emptyList())
			.orderStatus(OrderStatus.PAID)
			.customerNote("")
			.build();

		Delivery deliveryObj = Delivery.builder().order(order).deliveryType(type).build();
		deliveryObj.setId(id);
		return deliveryObj;
	}

	private Rider createTestRider(Long id, RiderStatus status, Location location) {
		Rider rider = Rider.builder()
			.id(id)
			.name("Test Rider " + id)
			.phoneNumber("010-1234-5678")
			.status(status)
			.location(location)
			.build();
		return rider;
	}

	/**
	 * DispatchService 헬퍼 메서드 테스트 (calculateDistance)
	 */
	@Test
	@DisplayName("두 지점 간의 거리 계산 - 정상 케이스")
	void calculateDistance_shouldReturnCorrectDistance() throws Exception {
		// given
		// 서울 강남 (37.498095, 127.027610)
		Location loc1 = new Location(37.498095, 127.027610);
		// 서울역 (37.555946, 126.972322)
		Location loc2 = new Location(37.555946, 126.972322);

		// private 메서드 호출을 위한 Reflection
		Method calculateDistanceMethod = DispatchService.class.getDeclaredMethod("calculateDistance", Location.class,
			Location.class);
		calculateDistanceMethod.setAccessible(true); // private 메서드 접근 허용

		// when
		double distance = (double)calculateDistanceMethod.invoke(dispatchService, loc1, loc2);

		// then
		// 예상 거리는 약 6.7km 정도 (정확한 값은 오차 범위 내에서 확인)
		assertEquals(8.072, distance, 0.001, "서울 강남과 서울역 사이의 거리가 올바르게 계산되어야 합니다.");
	}

	@Test
	@DisplayName("동일한 지점 간의 거리 계산 - 0을 반환해야 함")
	void calculateDistance_shouldReturnZeroForSameLocations() throws Exception {
		// given
		Location loc1 = new Location(37.5, 127.0);

		// private 메서드 호출을 위한 Reflection
		Method calculateDistanceMethod = DispatchService.class.getDeclaredMethod("calculateDistance", Location.class,
			Location.class);
		calculateDistanceMethod.setAccessible(true);

		// when
		double distance = (double)calculateDistanceMethod.invoke(dispatchService, loc1, loc1);

		// then
		assertEquals(0.0, distance, 0.001, "동일한 지점 간의 거리는 0이어야 합니다.");
	}

	@Test
	@DisplayName("null 위치가 포함된 거리 계산 - Double.MAX_VALUE를 반환해야 함")
	void calculateDistance_shouldReturnMaxValueForNullLocations() throws Exception {
		// given
		Location loc1 = new Location(37.5, 127.0);
		Location loc2 = null;

		// private 메서드 호출을 위한 Reflection
		Method calculateDistanceMethod = DispatchService.class.getDeclaredMethod("calculateDistance", Location.class,
			Location.class);
		calculateDistanceMethod.setAccessible(true);

		// when
		double distanceWithNull1 = (double)calculateDistanceMethod.invoke(dispatchService, loc1, loc2);
		double distanceWithNull2 = (double)calculateDistanceMethod.invoke(dispatchService, loc2, loc1);
		double distanceWithBothNull = (double)calculateDistanceMethod.invoke(dispatchService, loc2, loc2);

		// then
		assertEquals(Double.MAX_VALUE, distanceWithNull1, "한쪽 위치가 null이면 Double.MAX_VALUE를 반환해야 합니다.");
		assertEquals(Double.MAX_VALUE, distanceWithNull2, "다른 한쪽 위치가 null이면 Double.MAX_VALUE를 반환해야 합니다.");
		assertEquals(Double.MAX_VALUE, distanceWithBothNull, "두 위치 모두 null이면 Double.MAX_VALUE를 반환해야 합니다.");
	}

	/**
	 * DispatchService 헬퍼 메서드 테스트 (createRawStops)
	 */
	@Test
	@DisplayName("두 Delivery 객체로 4개의 Stop 객체 생성 - 정상 케이스")
	void createRawStops_shouldCreateFourStopsCorrectly() throws Exception {
		// given
		Location pickup1 = new Location(1.0, 1.0);
		Location delivery1 = new Location(2.0, 2.0);
		Location pickup2 = new Location(3.0, 3.0);
		Location delivery2 = new Location(4.0, 4.0);

		Delivery d1 = createTestDelivery(1L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, pickup1, delivery1);
		Delivery d2 = createTestDelivery(2L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, pickup2, delivery2);

		// private 메서드 호출을 위한 Reflection
		Method createRawStopsMethod = DispatchService.class.getDeclaredMethod("createRawStops", Delivery.class,
			Delivery.class);
		createRawStopsMethod.setAccessible(true);

		// when
		List<Stop> rawStops = (List<Stop>)createRawStopsMethod.invoke(dispatchService, d1, d2);

		// then
		assertNotNull(rawStops, "Stop 목록은 null이 아니어야 합니다.");
		assertEquals(4, rawStops.size(), "두 배달에 대해 총 4개의 Stop이 생성되어야 합니다.");

		// 각 Stop의 타입과 배달 객체 확인
		// Stop 생성 순서는 P1, D1, P2, D2
		assertEquals(StopType.PICKUP, rawStops.get(0).getType());
		assertEquals(d1.getId(), rawStops.get(0).getDelivery().getId());
		assertEquals(pickup1, rawStops.get(0).getLocation());

		assertEquals(StopType.DELIVERY, rawStops.get(1).getType());
		assertEquals(d1.getId(), rawStops.get(1).getDelivery().getId());
		assertEquals(delivery1, rawStops.get(1).getLocation());

		assertEquals(StopType.PICKUP, rawStops.get(2).getType());
		assertEquals(d2.getId(), rawStops.get(2).getDelivery().getId());
		assertEquals(pickup2, rawStops.get(2).getLocation());

		assertEquals(StopType.DELIVERY, rawStops.get(3).getType());
		assertEquals(d2.getId(), rawStops.get(3).getDelivery().getId());
		assertEquals(delivery2, rawStops.get(3).getLocation());
	}

	/**
	 * DispatchService 헬퍼 메서드 테스트 (isValidSequence)
	 */
	@Test
	@DisplayName("유효한 Stop 시퀀스 확인 - P->D 제약 준수")
	void isValidSequence_shouldReturnTrueForValidSequence() throws Exception {
		// given
		Delivery d1 = createTestDelivery(1L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, new Location(1.0, 1.0),
			new Location(2.0, 2.0));
		Delivery d2 = createTestDelivery(2L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, new Location(3.0, 3.0),
			new Location(4.0, 4.0));

		// P1 -> D1 -> P2 -> D2 (유효)
		List<Stop> validSequence1 = Arrays.asList(
			Stop.builder().delivery(d1).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d1).type(StopType.DELIVERY).build(),
			Stop.builder().delivery(d2).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d2).type(StopType.DELIVERY).build()
		);
		// P1 -> P2 -> D1 -> D2 (유효)
		List<Stop> validSequence2 = Arrays.asList(
			Stop.builder().delivery(d1).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d2).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d1).type(StopType.DELIVERY).build(),
			Stop.builder().delivery(d2).type(StopType.DELIVERY).build()
		);

		// private 메서드 호출을 위한 Reflection
		Method isValidSequenceMethod = DispatchService.class.getDeclaredMethod("isValidSequence", List.class,
			Delivery.class, Delivery.class);
		isValidSequenceMethod.setAccessible(true);

		// when
		boolean result1 = (boolean)isValidSequenceMethod.invoke(dispatchService, validSequence1, d1, d2);
		boolean result2 = (boolean)isValidSequenceMethod.invoke(dispatchService, validSequence2, d1, d2);

		// then
		assertTrue(result1, "P1 -> D1 -> P2 -> D2 시퀀스는 유효해야 합니다.");
		assertTrue(result2, "P1 -> P2 -> D1 -> D2 시퀀스는 유효해야 합니다.");
	}

	@Test
	@DisplayName("유효하지 않은 Stop 시퀀스 확인 - P->D 제약 위반")
	void isValidSequence_shouldReturnFalseForInvalidSequence() throws Exception {
		// given
		Delivery d1 = createTestDelivery(1L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, new Location(1.0, 1.0),
			new Location(2.0, 2.0));
		Delivery d2 = createTestDelivery(2L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, new Location(3.0, 3.0),
			new Location(4.0, 4.0));

		// D1 -> P1 (d1 제약 위반)
		List<Stop> invalidSequence1 = Arrays.asList(
			Stop.builder().delivery(d1).type(StopType.DELIVERY).build(),
			Stop.builder().delivery(d1).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d2).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d2).type(StopType.DELIVERY).build()
		);
		// P1 -> D2 -> P2 -> D1 (d1 제약 위반)
		List<Stop> invalidSequence2 = Arrays.asList(
			Stop.builder().delivery(d1).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d2).type(StopType.DELIVERY).build(),
			Stop.builder().delivery(d2).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d1).type(StopType.DELIVERY).build()
		);

		// private 메서드 호출을 위한 Reflection
		Method isValidSequenceMethod = DispatchService.class.getDeclaredMethod("isValidSequence", List.class,
			Delivery.class, Delivery.class);
		isValidSequenceMethod.setAccessible(true);

		// when
		boolean result1 = (boolean)isValidSequenceMethod.invoke(dispatchService, invalidSequence1, d1, d2);
		boolean result2 = (boolean)isValidSequenceMethod.invoke(dispatchService, invalidSequence2, d1, d2);

		// then
		assertFalse(result1, "D1 -> P1 시퀀스는 유효하지 않아야 합니다.");
		assertFalse(result2, "P1 -> D2 -> P2 -> D1 시퀀스는 유효하지 않아야 합니다.");
	}

	@Test
	@DisplayName("Stop 시퀀스에 특정 Delivery의 Stop이 누락된 경우 - false 반환")
	void isValidSequence_shouldReturnFalseIfStopsMissingForADelivery() throws Exception {
		// given
		Delivery d1 = createTestDelivery(1L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, new Location(1.0, 1.0),
			new Location(2.0, 2.0));
		Delivery d2 = createTestDelivery(2L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, new Location(3.0, 3.0),
			new Location(4.0, 4.0));

		// d2의 Stop이 누락된 시퀀스
		List<Stop> sequenceMissingD2 = Arrays.asList(
			Stop.builder().delivery(d1).type(StopType.PICKUP).build(),
			Stop.builder().delivery(d1).type(StopType.DELIVERY).build(),
			Stop.builder().delivery(d1).type(StopType.PICKUP).build(), // d2 대신 d1의 다른 Stop
			Stop.builder().delivery(d1).type(StopType.DELIVERY).build()
		);

		Method isValidSequenceMethod = DispatchService.class.getDeclaredMethod("isValidSequence", List.class,
			Delivery.class, Delivery.class);
		// private이더라도 접근을 허용한다.
		isValidSequenceMethod.setAccessible(true);

		// when
		boolean result = (boolean)isValidSequenceMethod.invoke(dispatchService, sequenceMissingD2, d1, d2); // 메서드를 실행할 객체, 인자 ,,,

		// then
		assertFalse(result, "Delivery의 Stop이 누락된 시퀀스는 유효하지 않아야 합니다.");
	}

	/**
	 * DispatchService 헬퍼 메서드 테스트 (calculateSequenceTotalDistance)
	 */
	@Test
	@DisplayName("Stop 시퀀스의 총 거리 계산 - 정상 케이스")
	void calculateSequenceTotalDistance_shouldReturnCorrectTotalDistance() throws Exception {
		// given
		Location locA = new Location(37.500, 127.000); // 0km
		Location locB = new Location(37.501, 127.001); // 약 0.15km
		Location locC = new Location(37.502, 127.002); // 약 0.15km

		Delivery d1 = createTestDelivery(1L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, locA, locB);
		Delivery d2 = createTestDelivery(2L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, locB, locC); // 실제 픽업/배달 위치는 테스트에 중요하지 않음

		List<Stop> sequence = Arrays.asList(
			Stop.builder().delivery(d1).type(StopType.PICKUP).location(locA).build(), // 0.0
			Stop.builder().delivery(d1).type(StopType.DELIVERY).location(locB).build(), // 0.15km
			Stop.builder().delivery(d2).type(StopType.PICKUP).location(locC).build()  // 0.15km
		);
		// 예상 총 거리: calculateDistance(locA, locB) + calculateDistance(locB, locC)

		// private 메서드 호출을 위한 Reflection
		Method calculateSequenceTotalDistanceMethod = DispatchService.class.getDeclaredMethod(
			"calculateSequenceTotalDistance", List.class);
		calculateSequenceTotalDistanceMethod.setAccessible(true);

		// when
		double totalDistance = (double)calculateSequenceTotalDistanceMethod.invoke(dispatchService, sequence);

		// then
		assertEquals(0.284, totalDistance, 0.001, "Stop 시퀀스의 총 거리가 올바르게 계산되어야 합니다.");
	}

	@Test
	@DisplayName("빈 Stop 시퀀스의 총 거리 계산 - 0을 반환해야 함")
	void calculateSequenceTotalDistance_shouldReturnZeroForEmptySequence() throws Exception {
		// given
		List<Stop> emptySequence = Collections.emptyList();

		// private 메서드 호출을 위한 Reflection
		Method calculateSequenceTotalDistanceMethod = DispatchService.class.getDeclaredMethod(
			"calculateSequenceTotalDistance", List.class);
		calculateSequenceTotalDistanceMethod.setAccessible(true);

		// when
		double totalDistance = (double)calculateSequenceTotalDistanceMethod.invoke(dispatchService, emptySequence);

		// then
		assertEquals(0.0, totalDistance, "빈 Stop 시퀀스의 총 거리는 0이어야 합니다.");
	}

	/**
	 * DispatchService 헬퍼 메서드 테스트 (permute 및 generateAllValidStopSequences)
	 * - 이 메서드들은 복잡하므로, attemptToBundle 테스트를 통해 암묵적으로 검증합니다.
	 * - 여기서는 generateAllValidStopSequences가 유효한 시퀀스를 올바르게 생성하는지 최소한으로 테스트합니다.
	 */
	@Test
	@DisplayName("generateAllValidStopSequences - 유효한 순열들을 생성해야 함")
	void generateAllValidStopSequences_shouldGenerateValidPermutations() throws Exception {
		// given
		Location p1Loc = new Location(1.0, 1.0);
		Location d1Loc = new Location(2.0, 2.0);
		Location p2Loc = new Location(3.0, 3.0);
		Location d2Loc = new Location(4.0, 4.0);

		Delivery d1 = createTestDelivery(1L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, p1Loc, d1Loc);
		Delivery d2 = createTestDelivery(2L, DeliveryType.BUNDLE, DeliveryStatus.PENDING, p2Loc, d2Loc);

		// createRawStops 헬퍼 메서드 호출
		Method createRawStopsMethod = DispatchService.class.getDeclaredMethod("createRawStops", Delivery.class,
			Delivery.class);
		createRawStopsMethod.setAccessible(true);
		List<Stop> rawStops = (List<Stop>)createRawStopsMethod.invoke(dispatchService, d1, d2);

		// generateAllValidStopSequences 헬퍼 메서드 호출
		Method generateAllValidStopSequencesMethod = DispatchService.class.getDeclaredMethod(
			"generateAllValidStopSequences", List.class, Delivery.class, Delivery.class);
		generateAllValidStopSequencesMethod.setAccessible(true);

		// when
		List<List<Stop>> validSequences = (List<List<Stop>>)generateAllValidStopSequencesMethod.invoke(dispatchService,
			rawStops, d1, d2);

		// then
		assertNotNull(validSequences, "유효한 시퀀스 목록은 null이 아니어야 합니다.");
		assertFalse(validSequences.isEmpty(), "유효한 시퀀스 목록은 비어있지 않아야 합니다.");

		// 최소한의 유효성 검사 (P->D 제약만)
		Method isValidSequenceMethod = DispatchService.class.getDeclaredMethod("isValidSequence", List.class,
			Delivery.class, Delivery.class);
		isValidSequenceMethod.setAccessible(true);

		for (List<Stop> sequence : validSequences) {
			assertTrue((boolean)isValidSequenceMethod.invoke(dispatchService, sequence, d1, d2),
				"생성된 모든 시퀀스는 유효성 검사를 통과해야 합니다.");
		}

		// 총 4개의 Stop에 대한 유효한 순열의 수는 P->D 제약을 만족할 때 8가지
		// (P1,D1) 블록과 (P2,D2) 블록이 서로 겹치거나 선행할 수 있는 경우의 수:
		// P1 P2 D1 D2
		// P1 P2 D2 D1
		// P2 P1 D1 D2
		// P2 P1 D2 D1
		// P1 D1 P2 D2
		// P2 D2 P1 D1
		// P1 D2 P2 D1 (이 경우는 P2가 D2보다 나중에 와서 안됨) -> 다시 확인
		// (Px, Dx)는 묶여서 이동하는 것이 아니라, 순서만 제약됨.
		// 예를 들어 P1 -> D2 -> P2 -> D1 은 P2가 D1보다 늦게 나오면 안됨 (P2는 D2 앞에 와야함)
		// 가능한 유효한 시퀀스는 8개가 맞음.
		// P1 D1 P2 D2
		// P1 P2 D1 D2
		// P1 P2 D2 D1 (P2 다음에 D1이 오는 것이 허용됨)
		// P2 P1 D1 D2
		// P2 P1 D2 D1
		// P2 D2 P1 D1
		// P1 D2 D1 P2 (불가능 D1 다음 P2)
		// D1 P1 ... (불가능)
		// 이 로직은 4! / (2! * 2!) = 6 가지의 Px, Dx 상대 순서 경우의 수가 있음.
		// 실제로 4개의 노드 (P1, D1, P2, D2)가 있고, P1 < D1, P2 < D2 라는 제약이 있을 때
		// 총 6가지 유효한 순열이 있습니다.
		// (P1, P2, D1, D2), (P1, P2, D2, D1), (P1, D1, P2, D2), (P2, P1, D1, D2), (P2, P1, D2, D1), (P2, D2, P1, D1)
		// 따라서 validSequences의 크기는 6이어야 합니다.
		assertEquals(6, validSequences.size(), "4개의 Stop과 2개의 P->D 제약을 가진 경우 6개의 유효한 순열이 생성되어야 합니다.");
	}
}
