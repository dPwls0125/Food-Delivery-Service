package personal.yejin.foodDelivery.domain.rider.model;

import lombok.Builder;
import lombok.Getter;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Getter
public class Rider extends GlobalEntity {
    private String name;
    private String phoneNumber;
    private RiderStatus status;
    private Location location;

    @Builder
    public Rider(String name, String phoneNumber, RiderStatus status, Location location) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.location = location;
    }

    public void updateLocation(double latitude, double longitude) {
        this.location = new Location(latitude, longitude);
    }

    public void setStatus(RiderStatus status) {
        this.status = status;
    }
}
