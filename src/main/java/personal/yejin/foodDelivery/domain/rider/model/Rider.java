package personal.yejin.foodDelivery.domain.rider.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "riders")
public class Rider extends GlobalEntity {
    private String name;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private RiderStatus status;

    @Embedded
    private Location location;

    public void updateLocation(double latitude, double longitude) {
        this.location = new Location(latitude, longitude);
        this.setUpdatedAt(LocalDateTime.now()); // Assuming GlobalEntity has setUpdatedAt
    }

    public void setStatus(RiderStatus status) {
        this.status = status;
        this.setUpdatedAt(LocalDateTime.now()); // Assuming GlobalEntity has setUpdatedAt
    }
}
