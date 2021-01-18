package net.sf.persism.dao.northwind;

import net.sf.persism.annotations.Column;

import java.math.BigDecimal;

/**
 * Comments for OrderDetail go here.
 *
 * @author Dan Howard
 * @since 5/24/12 4:39 PM
 */
public class OrderDetail {

    /*
	[OrderID] [int] NOT NULL,
	[ProductID] [int] NOT NULL,
	[UnitPrice] [money] NOT NULL,
	[Quantity] [smallint] NOT NULL,
	[Discount] [real] NOT NULL,
     */

    //@Column(primary = true)
    private int orderId;

    //@Column(primary = true)
    private int productId;

    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal discount;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}
