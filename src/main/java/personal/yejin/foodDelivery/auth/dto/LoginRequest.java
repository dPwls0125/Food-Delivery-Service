package personal.yejin.foodDelivery.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
