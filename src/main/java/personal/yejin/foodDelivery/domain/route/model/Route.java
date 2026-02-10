package personal.yejin.foodDelivery.domain.route.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.rider.model.Rider;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Route extends GlobalEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Stop> stops = new ArrayList<>();

    public static Route createRouteWithoutRider(List<Stop> stops) {
        Route route = Route.builder().build(); // Route 객체 생성 시 빌더 사용
        for (Stop stop : stops) {
            route.addStop(stop); // Stop을 추가하는 헬퍼 메서드를 사용하도록 변경
        }
        return route;
    }

    public void addStop(Stop stop) {
        this.stops.add(stop);
        stop.assignRoute(this); // Stop 엔티티에도 Route 참조 설정 (양방향 관계 관리)
    }

    public void assignRider(Rider rider) {
        this.rider = rider;
    }

    public Stop getStartLocation() {
        if(stops.isEmpty()){
            throw new IllegalStateException("Route의 경로가 지정되지 않았습니다.");
        }
        return stops.get(0);
    }

}
