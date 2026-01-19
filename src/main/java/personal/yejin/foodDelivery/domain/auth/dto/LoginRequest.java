package personal.yejin.foodDelivery.domain.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
