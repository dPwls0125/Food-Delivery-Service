package personal.yejin.foodDelivery.domain.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import personal.yejin.foodDelivery.domain.common.GlobalEntity;

@Getter
@Setter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends GlobalEntity {
    private Long menuId;
    private String menuName;
    private int quantity;
    private int unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Builder
    public OrderItem(Long menuId, String menuName, int quantity, int unitPrice) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order!=null && !order.getOrderItems().contains(this)) {
            order.getOrderItems().add(this);
        }
    }
}
