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
            SELECT r.*, sub.distance_val
            FROM riders r
            JOIN (
                SELECT r_inner.id,
                       (
                           6371 * acos(
                           GREATEST(-1, LEAST(1,
                               cos(radians(:startLatitude)) * cos(radians(r_inner.latitude)) *
                               cos(radians(r_inner.longitude) - radians(:startLongitude)) +
                               sin(radians(:startLatitude)) * sin(radians(r_inner.latitude))
                           ))
                       )
                       ) AS distance_val
                FROM riders r_inner
                WHERE r_inner.status = :status
                  AND r_inner.latitude IS NOT NULL
                  AND r_inner.longitude IS NOT NULL
            ) sub ON r.id = sub.id
            WHERE sub.distance_val IS NOT NULL
            ORDER BY sub.distance_val ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Rider> findNearestRiderByStatus(
            @Param("status") String status,
            @Param("startLatitude") double startLatitude,
            @Param("startLongitude") double startLongitude);}
