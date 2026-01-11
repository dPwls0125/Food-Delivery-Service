package personal.yejin.foodDelivery.auth.dto;

import java.time.LocalDateTime;

public record SignUpResponse(
        long id,
        String email,
        String name,
        String nickname,
        LocalDateTime createdAt
) {
}
