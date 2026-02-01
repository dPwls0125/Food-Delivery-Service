package personal.yejin.foodDelivery.domain.rider.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rider extends GlobalEntity {
    private String name;
    private String phoneNumber;
    private RiderStatus status;
    private Location location;

    public void updateLocation(double latitude, double longitude) {
        this.location = new Location(latitude, longitude);
    }

    public void setStatus(RiderStatus status) {
        this.status = status;
    }
}
