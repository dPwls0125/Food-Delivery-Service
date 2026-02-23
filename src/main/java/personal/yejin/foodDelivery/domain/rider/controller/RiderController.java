package personal.yejin.foodDelivery.domain.rider.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationRequest;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationResponse;
import personal.yejin.foodDelivery.domain.rider.service.RiderDispatchNotificationService;
import personal.yejin.foodDelivery.domain.rider.service.RiderService;

import java.net.URI;

@RestController
@RequestMapping("/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderDispatchNotificationService riderDispatchNotificationService;
    private final RiderService riderService;

    @GetMapping("/{riderId}/location")
    public ResponseEntity<RiderLocationResponse> getRiderLocation(@PathVariable Long riderId) {
        RiderLocationResponse data = riderService.getRiderLocation(riderId);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/{riderId}/location")
    public ResponseEntity<RiderLocationResponse> updateRiderLocation(@PathVariable Long riderId, @RequestBody RiderLocationRequest request) {
        RiderLocationResponse data = riderService.updateRiderLocation(riderId, request.latitude(), request.longitude());
        return ResponseEntity.created(URI.create("/api/riders/" + riderId + "/location"))
                .body(data);
    }

    @GetMapping(path = "/{riderId}/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long riderId) {
        return riderDispatchNotificationService.subscribeRiderNotification(riderId);
    }

}
