package personal.yejin.foodDelivery.domain.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long menuId;
    private String menuName; // 상세 조회 API에 포함된 메뉴명
    private int quantity;
    private int unitPrice; // 총 가격 계산을 위해 가정
}
