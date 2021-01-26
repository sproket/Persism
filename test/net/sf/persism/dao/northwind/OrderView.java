package net.sf.persism.dao.northwind;

import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Comments for OrderView go here.
 *
 * @author Dan Howard
 * @since 5/25/12 5:59 AM
 */
@NotTable
public class OrderView {

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

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(Date requiredDate) {
        this.requiredDate = requiredDate;
    }

    public Date getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(Date shippedDate) {
        this.shippedDate = shippedDate;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getJunk() {
        return junk;
    }

    public void setJunk(int junk) {
        this.junk = junk;
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
