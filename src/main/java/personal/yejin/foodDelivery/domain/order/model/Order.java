package personal.yejin.foodDelivery.domain.order.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.rider.model.Location;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Order extends GlobalEntity {

    @Column(nullable = false, name = "store_id")
    private Long storeId;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude"))
    })
    private Location pickupLocation; // 픽업 위치

    @Column(nullable = false)
    private String deliveryAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "delivery_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "delivery_longitude"))
    })
    private Location deliveryLocation; // 배달 위치

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    private int foodPrice;
    private int deliveryFee;
    private int finalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private String customerNote; // 상세 조회 API에 포함된 고객 요청

    public void updatePrices(int foodPrice, int deliveryFee, int discountAmount) {
        this.foodPrice = foodPrice;
        this.deliveryFee = deliveryFee;
        this.finalPrice = foodPrice + deliveryFee - discountAmount;
    }
}
