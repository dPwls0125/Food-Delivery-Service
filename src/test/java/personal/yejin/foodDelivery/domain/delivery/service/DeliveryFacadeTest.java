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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryFacadeTest {

    @Mock
    private RiderService riderService;
    @Mock
    private RouteService routeService;
    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
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
        Rider mockRider = Rider.builder().id(1L).name("Test Rider").build();

        Route mockRoute = Mockito.mock(Route.class); // Route를 mock으로 생성
        when(mockRoute.getId()).thenReturn(10L); // mockRoute.getId() Mocking 추가
        Stop mockStop = Mockito.mock(Stop.class); // Stop도 Mock으로 생성
        when(mockStop.getLocation()).thenReturn(pickupLocation); // Stop.getLocation() Mocking
        when(mockRoute.getStartLocation()).thenReturn(mockStop); // getStartLocation Mocking 추가

        when(routeService.createSingleRoute(any(Delivery.class))).thenReturn(mockRoute);
        when(riderService.assignRider(any(Location.class))).thenReturn(mockRider);

        // When
        Optional<Route> resultRoute = deliveryFacade.createSingleDelivery(mockDelivery);

        // Then
        assertTrue(resultRoute.isPresent());
        assertEquals(mockRoute.getId(), resultRoute.get().getId());
        verify(routeService, times(1)).createSingleRoute(mockDelivery);
        verify(mockRoute, times(1)).getStartLocation();
        verify(riderService, times(1)).assignRider(mockStop.getLocation());
        verify(mockRoute, times(1)).assignRider(mockRider);
        verify(mockDelivery, times(1)).dispatch(mockRoute);

        // dispatch 후 상태 변화 검증
        assertEquals(DeliveryStatus.DISPATCHED, mockDelivery.getStatus());
        assertEquals(mockRider, mockDelivery.getRider());
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
        Rider mockRider = Rider.builder().id(1L).name("Test Rider").build();

        when(deliveryRepository.findByIdIsNotAndDeliveryTypeAndStatus(anyLong(), any(DeliveryType.class), any(DeliveryStatus.class)))
                .thenReturn(List.of(delivery2)); // delivery1의 후보로 delivery2 반환

        Route mockRoute = Mockito.mock(Route.class); // Route를 mock으로 생성
        when(mockRoute.getId()).thenReturn(20L); // mockRoute.getId() Mocking 추가
        Stop mockStop = Mockito.mock(Stop.class); // Stop도 Mock으로 생성
        when(mockStop.getLocation()).thenReturn(pickupLocation1); // Stop.getLocation() Mocking
        when(mockRoute.getStartLocation()).thenReturn(mockStop); // getStartLocation Mocking 추가

        when(routeService.getOptimalRouteWithoutRider(any(Delivery.class), any(Delivery.class))).thenReturn(mockRoute);
        when(riderService.assignRider(any(Location.class))).thenReturn(mockRider);

        // When
        Optional<Route> resultRoute = deliveryFacade.attemptToBundle(delivery1);

        // Then
        assertTrue(resultRoute.isPresent());
        assertEquals(mockRoute.getId(), resultRoute.get().getId());
        verify(deliveryRepository, times(1)).findByIdIsNotAndDeliveryTypeAndStatus(delivery1.getId(), DeliveryType.BUNDLE, DeliveryStatus.PENDING);
        verify(routeService, times(1)).getOptimalRouteWithoutRider(delivery1, delivery2);
        verify(mockRoute, times(1)).getStartLocation();
        verify(riderService, times(1)).assignRider(mockStop.getLocation());
        verify(mockRoute, times(1)).assignRider(mockRider);
        verify(delivery1, times(1)).dispatch(mockRoute);
        verify(delivery2, times(1)).dispatch(mockRoute);

        // dispatch 후 상태 변화 검증
        assertEquals(DeliveryStatus.DISPATCHED, delivery1.getStatus());
        assertEquals(mockRider, delivery1.getRider());
        assertEquals(DeliveryStatus.DISPATCHED, delivery2.getStatus());
        assertEquals(mockRider, delivery2.getRider());
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
        Optional<Route> resultRoute = deliveryFacade.attemptToBundle(mockDelivery);

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
        Optional<Route> resultRoute = deliveryFacade.attemptToBundle(mockDelivery);

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
        Optional<Route> resultRoute = deliveryFacade.attemptToBundle(delivery1);

        // Then
        assertFalse(resultRoute.isPresent());
        verify(deliveryRepository, times(1)).findByIdIsNotAndDeliveryTypeAndStatus(delivery1.getId(), DeliveryType.BUNDLE, DeliveryStatus.PENDING);
        verifyNoInteractions(routeService);
        verifyNoInteractions(riderService);
    }
}
