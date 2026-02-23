package personal.yejin.foodDelivery.domain.rider.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Location {
    private double latitude;  // 위도
    private double longitude; // 경도

    public double calculateDistanceInHaversineFormula(Location compare) {
        if (compare == null) {
            return Double.MAX_VALUE;
        }

        double lat1 = this.latitude;
        double lon1 = this.longitude;
        double lat2 = compare.getLatitude();
        double lon2 = compare.getLongitude();

        double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
