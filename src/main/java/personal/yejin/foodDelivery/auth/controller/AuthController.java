package personal.yejin.foodDelivery.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal.yejin.foodDelivery.auth.dto.LoginRequest;
import personal.yejin.foodDelivery.auth.dto.LoginResponse;
import personal.yejin.foodDelivery.auth.dto.SignUpRequest;
import personal.yejin.foodDelivery.auth.dto.SignUpResponse;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/sign")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = new SignUpResponse(
                1L,
                "user@example.com",
                "김예진",
                "yejin",
                LocalDateTime.parse("2026-01-05T12:30:00")
        );
        return ResponseEntity.created(URI.create("/auth/sign/" + response.id())).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse(
                1L,
                LoginResponse.UserType.USER,
                "김예진"
        );
        return ResponseEntity.ok(response);
    }
}
