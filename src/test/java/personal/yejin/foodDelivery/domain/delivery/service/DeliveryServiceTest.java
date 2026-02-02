package personal.yejin.foodDelivery.domain.delivery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;
import personal.yejin.foodDelivery.domain.route.RouteService;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.model.StopType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;

    @Mock
    private RiderService riderService;

    @Mock
    private RouteService routeService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Test
    @DisplayName("단일 배송 성공")
    void testSingleDelivery() {
        // given
        Location pickupLocation = new Location(37.123, 127.123);
        Location deliveryLocation = new Location(37.456, 127.456);

        Order order = Order.builder()
                .pickupLocation(pickupLocation)
                .deliveryLocation(deliveryLocation)
                .build();

        Delivery delivery = Delivery.builder()
                .id(1L)
                .order(order)
                .build();

        Rider rider = Rider.builder()
                .id(1L)
                .name("Test Rider")
                .build();

        List<Stop> stops = new ArrayList<>();
        stops.add(Stop.builder().delivery(delivery).type(StopType.PICKUP).location(pickupLocation).sequence(1).build());
        stops.add(Stop.builder().delivery(delivery).type(StopType.DELIVERY).location(deliveryLocation).sequence(2).build());
        Route route = Route.createRouteWithoutRider(stops);

        when(routeService.createSingleRoute(any(Delivery.class))).thenReturn(route);
        when(riderService.assignRider(any(Location.class))).thenReturn(rider);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        // when
        Optional<Route> resultRouteOpt = deliveryService.singleDelivery(delivery);

        // then
        assertTrue(resultRouteOpt.isPresent());
        Route resultRoute = resultRouteOpt.get();

        assertEquals(rider.getId(), resultRoute.getRider().getId());
        assertEquals(2, resultRoute.getStops().size());
        assertEquals(StopType.PICKUP, resultRoute.getStops().get(0).getType());
        assertEquals(StopType.DELIVERY, resultRoute.getStops().get(1).getType());

        verify(routeService, times(1)).createSingleRoute(delivery);
        verify(riderService, times(1)).assignRider(pickupLocation);
        verify(deliveryRepository, times(1)).save(delivery);
    }
}
