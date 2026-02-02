package personal.yejin.foodDelivery.domain.route.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.repository.RouteRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    @Test
    @DisplayName("단일 배달 경로 생성 테스트")
    void testCreateSingleRoute() {
        // Given
        Location pickupLocation = new Location(37.1, 127.1);
        Location deliveryLocation = new Location(37.2, 127.2);

        Order mockOrder = Order.builder()
                .pickupLocation(pickupLocation)
                .deliveryLocation(deliveryLocation)
                .build();

        Delivery mockDelivery = Delivery.builder()
                .id(1L)
                .order(mockOrder)
                .build();

        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
            Route route = invocation.getArgument(0);
            if (route.getId() == null) {
                route.setId(1L); // 가짜 ID 할당
            }
            return route;
        });

        // When
        Route createdRoute = routeService.createSingleRoute(mockDelivery);

        // Then
        assertNotNull(createdRoute);
        assertNotNull(createdRoute.getId());
        assertEquals(2, createdRoute.getStops().size()); // Pickup stop + Delivery stop
        createdRoute.getStops().forEach(stop -> assertNotNull(stop.getRoute()));
    }

    @Test
    @DisplayName("최적의 묶음 배달 경로 생성 테스트")
    void testGetOptimalRouteWithoutRider() {
        // Given
        // Delivery 1
        Location pickupLoc1 = new Location(37.0, 127.0);
        Location deliveryLoc1 = new Location(37.1, 127.1);
        Order order1 = Order.builder()
                .pickupLocation(pickupLoc1)
                .deliveryLocation(deliveryLoc1)
                .build();
        Delivery delivery1 = Delivery.builder().id(1L).order(order1).build();

        // Delivery 2
        Location pickupLoc2 = new Location(37.2, 127.2);
        Location deliveryLoc2 = new Location(37.3, 127.3);
        Order order2 = Order.builder()
                .pickupLocation(pickupLoc2)
                .deliveryLocation(deliveryLoc2)
                .build();
        Delivery delivery2 = Delivery.builder().id(2L).order(order2).build();

        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
            Route route = invocation.getArgument(0);
            if (route.getId() == null) {
                route.setId(1L); // 가짜 ID 할당
            }
            return route;
        });

        // When
        Route optimalRoute = routeService.getOptimalRouteWithoutRider(delivery1, delivery2);

        // Then
        assertNotNull(optimalRoute);
        assertNotNull(optimalRoute.getId());
        assertEquals(4, optimalRoute.getStops().size()); // 2 * Pickup + 2 * Delivery
        optimalRoute.getStops().forEach(stop -> assertNotNull(stop.getRoute()));
        // 각 Stop의 sequence가 올바르게 설정되었는지 추가 검증 가능
    }
}
