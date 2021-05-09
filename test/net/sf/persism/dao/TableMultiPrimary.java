package net.sf.persism.dao;

import java.util.Date;

/**
 * Comments for TableMultiPrimary go here.
 * Similar to Northwind Order Detail - no autoinc instead OrderID and ProductID are primary.
 * @author danhoward
 * @since 12-05-21 6:19 AM
 */

public final class TableMultiPrimary {
    private int orderId;
    private String productId;
    private double unitPrice;
    private short quantity;
    private float discount;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "TableMultiPrimary{" +
                "orderId=" + orderId +
                ", productId=" + productId +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", discount=" + discount +
                "}\n";
    }
}
