package personal.yejin.foodDelivery.rider.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.rider.dto.RiderLocationRequest;
import personal.yejin.foodDelivery.rider.dto.RiderLocationResponse;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/riders")
public class RiderController {
    @GetMapping("/{riderId}/location")
    public ResponseEntity<RiderLocationResponse> getRiderLocation(@PathVariable Long riderId) {
        RiderLocationResponse response = new RiderLocationResponse(
                riderId,
                37.498095,
                127.027610,
                LocalDateTime.parse("2026-01-05T16:15:00")
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{riderId}/location")
    public ResponseEntity<RiderLocationResponse> updateRiderLocation(@PathVariable Long riderId, @RequestBody RiderLocationRequest request){

        RiderLocationResponse response = new RiderLocationResponse(
                riderId,
                request.latitude(),
                request.longitude(),
                LocalDateTime.parse("2026-01-05T16:15:00")
        );

        return  ResponseEntity.created(URI.create("/api/riders/" + riderId + "/location"))
                .body(response);
    }
}
