package personal.yejin.foodDelivery.auth.dto;

public record SignUpRequest(
        String email,
        String password,
        String name,
        SignUpType signupType
) {
    public enum SignUpType {
        USER, OWNER
    }
}
