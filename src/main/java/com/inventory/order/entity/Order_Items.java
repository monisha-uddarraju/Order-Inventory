package com.inventory.order.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

//import java.io.Serializable;
import java.math.BigDecimal;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items",
       indexes = {
           @Index(name = "idx_order_items_order", columnList = "order_id"),
           @Index(name = "idx_order_items_product", columnList = "product_id"),
           @Index(name = "idx_order_items_shipment", columnList = "shipment_id")
       })
@ToString(exclude = {"order", "product", "shipment"})
//@IdClass(Order_Items.OrderItemId.class)
public class Order_Items {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_items_order"))
    private Orders order;

    @Id
    @Column(name = "line_item_id", nullable = false)
    private Integer lineItemId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_items_product"))
    private Products product;

    @PositiveOrZero
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id",
                foreignKey = @ForeignKey(name = "fk_order_items_shipment"))
    private Shipments shipment;

//    /**
//     * Composite key class for (order_id, line_item_id).
//     * Note: for @IdClass, field names must match the entity IDs and
//     * the type of relationship IDs use the FK's PK type (Order.id → Long).
//     */
//    @Getter @Setter
//    @NoArgsConstructor @AllArgsConstructor
//    @EqualsAndHashCode
//    
//    public static class OrderItemId implements Serializable {
//        /**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		private Long order;       // matches 'order' field's PK type
//        private Long lineItemId;  // matches 'lineItemId'
//    }
}