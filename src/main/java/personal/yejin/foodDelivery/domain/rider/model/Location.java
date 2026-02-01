package personal.yejin.foodDelivery.domain.rider.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private double latitude;  // 위도
    private double longitude; // 경도
}
