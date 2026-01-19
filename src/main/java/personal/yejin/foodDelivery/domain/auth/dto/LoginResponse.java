package personal.yejin.foodDelivery.domain.auth.dto;

public record LoginResponse(
        long userId,
        UserType userType,
        String name
) {
    public enum UserType {
        USER, OWNER
    }
}
