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

import java.util.concurrent.CompletableFuture;

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
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(data);
    }

    @GetMapping(path = "/{riderId}/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long riderId) {
        return riderDispatchNotificationService.subscribeRiderNotification(riderId);
    }

    @PostMapping("/test")
    public CompletableFuture<String> test() {

        System.out.println("Controller thread = " + Thread.currentThread().getName());

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Async thread = " + Thread.currentThread().getName());
            return "done";
        });
    }


    @PostMapping("/join-test")
    public String joinTest() {
        System.out.println("Controller thread (start) = " + Thread.currentThread().getName());

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Async thread = " + Thread.currentThread().getName());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
            return "done";
        });

        System.out.println("Controller thread (before join) = " + Thread.currentThread().getName());

        String result = future.join();

        System.out.println("Controller thread (after join) = " + Thread.currentThread().getName());

        return result;
    }

}
