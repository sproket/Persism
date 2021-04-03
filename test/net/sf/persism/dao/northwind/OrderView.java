package net.sf.persism.dao.northwind;

import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * test for Record style where getters don't start with "get" and instead match the field name.
 *
 * @author Dan Howard
 * @since 5/25/12 5:59 AM
 */
@NotTable
public final class OrderView {

    private int orderId;
    private String customerId;
    private int employeeId;
    private Date orderDate;
    private Date requiredDate;
    private Date shippedDate;

    private int productId;
    private float unitPrice;
    private int quantity;
    private BigDecimal discount;

    private String customerName;
    private String employeeName;
    private String productName;

    @NotColumn
    private int junk;

    public int orderId() {
        return orderId;
    }

    public String customerId() {
        return customerId;
    }

    public int employeeId() {
        return employeeId;
    }

    public Date orderDate() {
        return orderDate;
    }

    public Date requiredDate() {
        return requiredDate;
    }

    public Date shippedDate() {
        return shippedDate;
    }

    public int productId() {
        return productId;
    }

    public float unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal discount() {
        return discount;
    }

    public String customerName() {
        return customerName;
    }

    public String employeeName() {
        return employeeName;
    }

    public String productName() {
        return productName;
    }

    @Override
    public String toString() {
        return "OrderView{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", productName='" + productName + '\'' +
                ", orderDate=" + orderDate +
                ", requiredDate=" + requiredDate +
                ", shippedDate=" + shippedDate +
                ", productId=" + productId +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", discount=" + discount +
                "\n}";
    }
}
