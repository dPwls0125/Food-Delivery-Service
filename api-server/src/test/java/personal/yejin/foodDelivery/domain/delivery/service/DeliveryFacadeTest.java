package personal.yejin.foodDelivery.domain.delivery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.service.RouteService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryFacadeTest {

    @Mock
    private RiderService riderService;
    @Mock
    private RouteService routeService;
    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private DeliveryFacade deliveryFacade;

    @Test
    @DisplayName("단일 배송 처리 테스트")
    void singleDeliveryTest() {
        // Given
        Location pickupLocation = new Location(37.1, 127.1);
        Location deliveryLocation = new Location(37.2, 127.2);
        Order mockOrder = Order.builder()
                .pickupLocation(pickupLocation)
                .deliveryLocation(deliveryLocation)
                .build();
        Delivery mockDelivery = Mockito.spy(Delivery.builder() // spy 객체로 생성
                .id(1L)
                .order(mockOrder)
                .deliveryType(DeliveryType.SINGLE)
                .status(DeliveryStatus.PENDING)
                .build());
        Rider mockRider = Rider.builder().id(1L).name("TestLogging Rider").build();

        Route mockRoute = Mockito.mock(Route.class); // Route를 mock으로 생성
        Stop mockStop = Mockito.mock(Stop.class); // Stop도 Mock으로 생성
        when(mockStop.getLocation()).thenReturn(pickupLocation); // Stop.getLocation() Mocking
        when(mockRoute.getStartLocation()).thenReturn(mockStop); // getStartLocation Mocking 추가
        when(mockRoute.getRider()).thenReturn(mockRider); // Mock getRider() for mockRoute

        when(routeService.createSingleRoute(any(Delivery.class))).thenReturn(mockRoute);
        when(riderService.assignRiderOptimized(any(Location.class))).thenReturn(mockRider);
        doAnswer(invocation -> {
            Route routeArg = invocation.getArgument(0);
            Rider riderArg = invocation.getArgument(1);
            Delivery deliveryArg = invocation.getArgument(2);
            deliveryArg.dispatch(routeArg); // Simulate the real dispatchSingle behavior
            return deliveryArg;
        }).when(deliveryService).dispatchSingle(any(Route.class), any(Rider.class), eq(mockDelivery)); // Mock dispatchSingle to return the passed-in delivery and call dispatch on it

        // When
        Optional<Delivery> resultRoute = deliveryFacade.createSingleDelivery(mockDelivery);

        // Then
        assertTrue(resultRoute.isPresent());
        assertEquals(mockDelivery.getId(), resultRoute.get().getId()); // Delivery should retain its own ID
        verify(routeService, times(1)).createSingleRoute(mockDelivery);
        verify(mockRoute, times(1)).getStartLocation();
        verify(riderService, times(1)).assignRiderOptimized(mockStop.getLocation());
        verify(mockDelivery, times(1)).dispatch(mockRoute);

        // dispatch 후 상태 변화 검증
        assertEquals(DeliveryStatus.DISPATCHED, mockDelivery.getStatus());
        verify(mockDelivery, times(1)).setRider(mockRider);
    }

    @Test
    @DisplayName("묶음 배송 시도 테스트 - 성공")
    void attemptToBundleSuccessTest() {
        // Given
        Location pickupLocation1 = new Location(37.1, 127.1);
        Location deliveryLocation1 = new Location(37.2, 127.2);
        Order order1 = Order.builder()
                .pickupLocation(pickupLocation1)
                .deliveryLocation(deliveryLocation1)
                .build();
        Delivery delivery1 = Mockito.spy(Delivery.builder() // spy 객체로 생성
                .id(1L)
                .order(order1)
                .deliveryType(DeliveryType.BUNDLE)
                .status(DeliveryStatus.PENDING)
                .build());
        // Force spies to call real methods for status/rider updates
        doCallRealMethod().when(delivery1).setRider(any(Rider.class));
        doCallRealMethod().when(delivery1).updateStatus(any(DeliveryStatus.class));
        doCallRealMethod().when(delivery1).getRider();
        doCallRealMethod().when(delivery1).getStatus();


        Location pickupLocation2 = new Location(37.1001, 127.1001); // Within BUNDLE_RADIUS_KM
        Location deliveryLocation2 = new Location(37.2001, 127.2001);
        Order order2 = Order.builder()
                .pickupLocation(pickupLocation2)
                .deliveryLocation(deliveryLocation2)
                .build();
        Delivery delivery2 = Mockito.spy(Delivery.builder() // spy 객체로 생성
                .id(2L)
                .order(order2)
                .deliveryType(DeliveryType.BUNDLE)
                .status(DeliveryStatus.PENDING)
                .build());
        // Force spies to call real methods for status/rider updates
        doCallRealMethod().when(delivery2).setRider(any(Rider.class));
        doCallRealMethod().when(delivery2).updateStatus(any(DeliveryStatus.class));
        doCallRealMethod().when(delivery2).getRider();
        doCallRealMethod().when(delivery2).getStatus();

        Rider mockRider = Rider.builder().id(1L).name("TestLogging Rider").build();

        when(deliveryRepository.findByIdIsNotAndDeliveryTypeAndStatus(anyLong(), any(DeliveryType.class), any(DeliveryStatus.class)))
                .thenReturn(List.of(delivery2)); // delivery1의 후보로 delivery2 반환

        Route mockRoute = Mockito.mock(Route.class); // Route를 mock으로 생성
        Stop mockStop = Mockito.mock(Stop.class); // Stop도 Mock으로 생성
        when(mockStop.getLocation()).thenReturn(pickupLocation1); // Stop.getLocation() Mocking
        when(mockRoute.getStartLocation()).thenReturn(mockStop); // getStartLocation Mocking 추가
        when(mockRoute.getRider()).thenReturn(mockRider); // Mock getRider() for mockRoute

        when(routeService.getOptimalRouteWithoutRider(any(Delivery.class), any(Delivery.class))).thenReturn(mockRoute);
        when(riderService.assignRiderOptimized(any(Location.class))).thenReturn(mockRider);
        doAnswer(invocation -> {
            Route routeArg = invocation.getArgument(0);
            Rider riderArg = invocation.getArgument(1);
            Delivery delivery1Arg = invocation.getArgument(2);
            Delivery delivery2Arg = invocation.getArgument(3);
            System.out.println("Inside dispatchBundle doAnswer: delivery1Arg is " + (delivery1Arg != null ? "not null" : "null"));
            delivery1Arg.dispatch(routeArg); // Simulate the real dispatchBundle behavior for delivery1
            delivery2Arg.dispatch(routeArg); // Simulate the real dispatchBundle behavior for delivery2
            return delivery1Arg;
        }).when(deliveryService).dispatchBundle(any(Route.class), any(Rider.class), eq(delivery1), eq(delivery2));

        // When
        Optional<Delivery> resultRoute = deliveryFacade.attemptToBundle(delivery1);

        // Then
        verify(deliveryService, times(1)).dispatchBundle(any(Route.class), any(Rider.class), eq(delivery1), eq(delivery2)); // Verify dispatchBundle was called
        assertTrue(resultRoute.isPresent());
        System.out.println("delivery1.getId(): " + delivery1.getId());
        System.out.println("resultRoute.get().getId(): " + resultRoute.get().getId());
    }

    @Test
    @DisplayName("묶음 배송 시도 테스트 - 배송 타입 불일치")
    void attemptToBundleWrongDeliveryTypeTest() {
        // Given
        Delivery mockDelivery = Mockito.spy(Delivery.builder() // spy 객체로 생성
                .id(1L)
                .deliveryType(DeliveryType.SINGLE) // Not BUNDLE
                .status(DeliveryStatus.PENDING)
                .build());

        // When
        Optional<Delivery> resultRoute = deliveryFacade.attemptToBundle(mockDelivery);

        // Then
        assertFalse(resultRoute.isPresent());
        verifyNoInteractions(deliveryRepository);
        verifyNoInteractions(routeService);
        verifyNoInteractions(riderService);
    }

    @Test
    @DisplayName("묶음 배송 시도 테스트 - 배송 상태 불일치")
    void attemptToBundleWrongDeliveryStatusTest() {
        // Given
        Delivery mockDelivery = Mockito.spy(Delivery.builder() // spy 객체로 생성
                .id(1L)
                .deliveryType(DeliveryType.BUNDLE)
                .status(DeliveryStatus.DISPATCHED) // Not PENDING
                .build());

        // When
        Optional<Delivery> resultRoute = deliveryFacade.attemptToBundle(mockDelivery);

        // Then
        assertFalse(resultRoute.isPresent());
        verifyNoInteractions(deliveryRepository);
        verifyNoInteractions(routeService);
        verifyNoInteractions(riderService);
    }

    @Test
    @DisplayName("묶음 배송 시도 테스트 - 후보 배송 없음")
    void attemptToBundleNoCandidateTest() {
        // Given
        Location pickupLocation1 = new Location(37.1, 127.1);
        Location deliveryLocation1 = new Location(37.2, 127.2);
        Order order1 = Order.builder()
                .pickupLocation(pickupLocation1)
                .deliveryLocation(deliveryLocation1)
                .build();
        Delivery delivery1 = Mockito.spy(Delivery.builder() // spy 객체로 생성
                .id(1L)
                .order(order1)
                .deliveryType(DeliveryType.BUNDLE)
                .status(DeliveryStatus.PENDING)
                .build());

        when(deliveryRepository.findByIdIsNotAndDeliveryTypeAndStatus(anyLong(), any(DeliveryType.class), any(DeliveryStatus.class)))
                .thenReturn(List.of()); // 후보 없음 반환

        // When
        Optional<Delivery> resultRoute = deliveryFacade.attemptToBundle(delivery1);

        // Then
        assertFalse(resultRoute.isPresent());
        verify(deliveryRepository, times(1)).findByIdIsNotAndDeliveryTypeAndStatus(delivery1.getId(), DeliveryType.BUNDLE, DeliveryStatus.PENDING);
        verifyNoInteractions(routeService);
        verifyNoInteractions(riderService);
    }
}
