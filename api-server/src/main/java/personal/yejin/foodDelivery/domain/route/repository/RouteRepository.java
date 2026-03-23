package personal.yejin.foodDelivery.domain.route.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import personal.yejin.foodDelivery.domain.route.model.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

}
