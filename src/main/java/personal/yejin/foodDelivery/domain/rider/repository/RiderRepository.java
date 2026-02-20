package personal.yejin.foodDelivery.domain.rider.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;

public interface RiderRepository extends JpaRepository<Rider, Long> {
	List<Rider> findByStatus(RiderStatus status);

    @Query(value = """
            SELECT *
            FROM riders
            WHERE status = :status
              AND location_latitude BETWEEN :minLat AND :maxLat
              AND location_longitude BETWEEN :minLon AND :maxLon
            ORDER BY ST_Distance(
                POINT(location_longitude, location_latitude),
                POINT(:startLongitude, :startLatitude)
            ) ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Rider> findNearestRiderByStatusWithBounds(
            @Param("status") String status,
            @Param("startLatitude") double startLatitude,
            @Param("startLongitude") double startLongitude,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon);}
