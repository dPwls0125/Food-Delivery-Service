package personal.yejin.foodDelivery.domain.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import personal.yejin.foodDelivery.domain.delivery.dto.DeliveryStatusUpdateRequest;
import personal.yejin.foodDelivery.domain.delivery.dto.DeliveryStatusUpdateResponse;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchRequest;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchResponse;
import personal.yejin.foodDelivery.domain.delivery.service.DeliveryFacade;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryFacade deliveryFacade;
	@PostMapping("/{orderId}/dispatch")
	public ResponseEntity<DispatchResponse> requestDispatch(
		@PathVariable Long orderId,
		@RequestBody DispatchRequest request
	) {
		DispatchResponse response = deliveryFacade.dispatchRiderAndCreateDelivery(orderId, request.deliveryType());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{deliveryId}/status")
	public ResponseEntity<DeliveryStatusUpdateResponse> updateDeliveryStatus(
		@PathVariable Long deliveryId,
		@RequestBody DeliveryStatusUpdateRequest request
	) {
		DeliveryStatusUpdateResponse response = deliveryFacade.updateDeliveryStatus(deliveryId, request.status());
		return ResponseEntity.ok(response);
	}
}
