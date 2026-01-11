package personal.yejin.foodDelivery.route.dto;


import java.util.List;
public record CurrentRouteResponse (
    List<Stop> stops
)
{ }
