package personal.yejin.foodDelivery.domain.delivery.service;

import java.util.Comparator;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.route.model.Route;


@RequiredArgsConstructor
@Service
@Transactional(readOnly = true) // 클래스 레벨에 readOnly 트랜잭션 적용
public class DeliveryService {
	private static final double BUNDLE_RADIUS_KM = 2.0;

	private final DeliveryRepository deliveryRepository;

	public boolean isBundleEligible(Delivery delivery) {
		return delivery.getDeliveryType() == DeliveryType.BUNDLE
			&& delivery.getStatus() == DeliveryStatus.PENDING;
	}

	public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
		Delivery delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found with id: " + deliveryId));
		delivery.updateStatus(status);
		return delivery;
	}

	@Transactional
	public Delivery save(Delivery delivery) {
		return deliveryRepository.save(delivery);
	}

	public Optional<Delivery> findBundleCandidate(Delivery delivery) {
		Location pickupLocation1 = delivery.getOrder().getPickupLocation();

		return deliveryRepository.findByIdIsNotAndDeliveryTypeAndStatus(
				delivery.getId(),
				DeliveryType.BUNDLE,
				DeliveryStatus.PENDING
			).stream()
			.filter(candidate -> {
				Location pickupLocation2 = candidate.getOrder().getPickupLocation();
				double distance = pickupLocation1.calculateDistanceInHaversineFormula(pickupLocation2);
				return distance <= BUNDLE_RADIUS_KM;
			})
			.min(Comparator.comparingDouble(
				c -> pickupLocation1.calculateDistanceInHaversineFormula(c.getOrder().getPickupLocation())));
	}

	@Transactional
	public Delivery dispatchSingle(Route route, Rider rider, Delivery delivery) {
		route.assignRider(rider);
		delivery.dispatch(route);
		return delivery;
	}

	@Transactional
	public Delivery dispatchBundle(Route route, Rider rider, Delivery delivery1, Delivery delivery2) {
		route.assignRider(rider);
		delivery1.dispatch(route);
		delivery2.dispatch(route);
		return delivery1;
	}

}
