package personal.yejin.foodDelivery.domain.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.delivery.dto.DeliveryStatusUpdateResponse;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchResponse;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.model.DispatchStatus;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.service.OrderService;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.service.RiderDispatchNotificationService;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;
import personal.yejin.foodDelivery.domain.route.model.Route;
import personal.yejin.foodDelivery.domain.route.model.Stop;
import personal.yejin.foodDelivery.domain.route.service.RouteService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeliveryFacade {
    private final RiderService riderService;
    private final RouteService routeService;
    private final DeliveryService deliveryService;
    private final OrderService orderService;
	private final RiderDispatchNotificationService riderDispatchNotificationService;

    public DeliveryStatusUpdateResponse updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryService.updateDeliveryStatus(deliveryId, status);
        return new DeliveryStatusUpdateResponse(delivery.getOrder().getId(), delivery.getStatus());
    }


    @Transactional
    public DispatchResponse dispatchRiderAndCreateDelivery(Long orderId, DeliveryType deliveryType) {

        Order order = orderService.getOrderById(orderId);
        Delivery delivery = new Delivery(order, deliveryType);
        Optional<Delivery> dispatchedDeliveryOpt;

        if (deliveryType == DeliveryType.SINGLE) {
            dispatchedDeliveryOpt = createSingleDelivery(delivery);
        } else {
            dispatchedDeliveryOpt = attemptToBundle(delivery);
            if (dispatchedDeliveryOpt.isEmpty()) {
                dispatchedDeliveryOpt = createSingleDelivery(delivery);
            }
        }

        Delivery dispatchedDelivery = dispatchedDeliveryOpt.orElseThrow(
                () -> new IllegalStateException("배달 생성에 실패했습니다."));

		riderDispatchNotificationService.notifyDispatchAssigned(dispatchedDelivery);

        return new DispatchResponse(
                dispatchedDelivery.getOrder().getId(),
                dispatchedDelivery.getId(),
                DispatchStatus.REQUESTED,
                dispatchedDelivery.getDeliveryType()
        );
    }

    @Transactional
    public Optional<Delivery> createSingleDelivery(Delivery delivery) {
        Route route = routeService.createSingleRoute(delivery);
        Stop startPoint = (route.getStartLocation());
        Rider riderOptimal = riderService.assignRider(startPoint.getLocation());
        return Optional.of(deliveryService.dispatchSingle(route, riderOptimal, delivery));
    }

    @Transactional
    public Optional<Delivery> attemptToBundle(Delivery delivery1) {
        if (!deliveryService.isBundleEligible(delivery1)) {
            return Optional.empty();
        }

        Optional<Delivery> candidateOpt = deliveryService.findBundleCandidate(delivery1);
        if (candidateOpt.isEmpty()) {
            return Optional.empty();
        }

        Delivery delivery2 = candidateOpt.get();
        Route route = routeService.getOptimalRouteWithoutRider(delivery1, delivery2);
        Stop startPoint = route.getStartLocation();
        Rider riderOptimal = riderService.assignRider(startPoint.getLocation());

        return Optional.of(deliveryService.dispatchBundle(route, riderOptimal, delivery1, delivery2));
    }
}
