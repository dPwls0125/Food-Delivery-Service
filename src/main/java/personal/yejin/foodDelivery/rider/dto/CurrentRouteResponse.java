package personal.yejin.foodDelivery.rider.dto;


import java.util.List;
public record CurrentRouteResponse (
    List<Stop> stops
)
{
    public record Stop (
        int sequence,
        StopType type,
        long orderId,
        String address
    ){}

    public enum StopType {
        PICKUP, DELIVERY
    }
}
