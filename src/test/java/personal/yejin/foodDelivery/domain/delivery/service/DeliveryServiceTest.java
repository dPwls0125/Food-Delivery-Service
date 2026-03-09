package personal.yejin.foodDelivery.domain.delivery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.service.DelayCompensationCouponService;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.model.StopType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private DelayCompensationCouponService delayCompensationCouponService;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    @DisplayName("배달 시작(PICKED_UP) 시, 고지된 배달 시간 초과면 15% 보상 쿠폰을 발급한다")
    void issueDelayCouponWhenPickedUpAfterPromisedTime() {
        Delivery delivery = createDeliveryWithEstimatedDeliveryTime(LocalDateTime.now().minusMinutes(5));
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
        when(cacheManager.getCache("activateDeliveries")).thenReturn(cache);

        deliveryService.updateDeliveryStatus(1L, DeliveryStatus.PICKED_UP);

        verify(delayCompensationCouponService).issueForDelayedDelivery(10L);
        verify(cache).evict(100L);
    }

    @Test
    @DisplayName("배달 시작(PICKED_UP) 시, 고지된 배달 시간 이내면 보상 쿠폰을 발급하지 않는다")
    void doNotIssueDelayCouponWhenWithinPromisedTime() {
        Delivery delivery = createDeliveryWithEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(10));
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
        when(cacheManager.getCache("activateDeliveries")).thenReturn(cache);

        deliveryService.updateDeliveryStatus(1L, DeliveryStatus.PICKED_UP);

        verify(delayCompensationCouponService, never()).issueForDelayedDelivery(anyLong());
        verify(cache).evict(100L);
    }

    @Test
    @DisplayName("배달 시작 상태가 아니면 고지 시간 초과여도 보상 쿠폰을 발급하지 않는다")
    void doNotIssueDelayCouponWhenStatusIsNotPickedUp() {
        Delivery delivery = createDeliveryWithEstimatedDeliveryTime(LocalDateTime.now().minusMinutes(5));
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
        when(cacheManager.getCache("activateDeliveries")).thenReturn(cache);

        deliveryService.updateDeliveryStatus(1L, DeliveryStatus.DELIVERED);

        verify(delayCompensationCouponService, never()).issueForDelayedDelivery(anyLong());
        verify(cache).evict(100L);
    }

    private Delivery createDeliveryWithEstimatedDeliveryTime(LocalDateTime estimatedTime) {
        Order order = Order.builder().id(10L).build();
        Rider rider = Rider.builder().id(100L).build();

        Delivery delivery = Delivery.builder()
                .id(1L)
                .order(order)
                .rider(rider)
                .status(DeliveryStatus.DISPATCHED)
                .build();

        Stop deliveryStop = Stop.builder()
                .delivery(delivery)
                .type(StopType.DELIVERY)
                .estimatedTime(estimatedTime)
                .sequence(2)
                .build();

        Route route = Route.builder()
                .stops(List.of(deliveryStop))
                .build();

        return Delivery.builder()
                .id(1L)
                .order(order)
                .rider(rider)
                .route(route)
                .status(DeliveryStatus.DISPATCHED)
                .build();
    }
}
