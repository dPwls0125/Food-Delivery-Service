package personal.yejin.foodDelivery.auth.dto;

public record LoginResponse(
        long userId,
        UserType userType,
        String name
) {
    public enum UserType {
        USER, OWNER
    }
}
