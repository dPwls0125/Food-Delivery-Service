package personal.yejin.foodDelivery.domain.route.dto;


import java.util.List;
public record CurrentRouteResponse (
    List<Stop> stops
)
{ }
