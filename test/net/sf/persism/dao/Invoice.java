package net.sf.persism.dao;

import net.sf.persism.annotations.NotColumn;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: danhoward
 * Date: 12-05-15
 * Time: 4:43 PM
 */
public class Invoice {

    private int invoiceId;
    private String customerId;
    private float price;
    private int quantity;
    private double discount;

    @NotColumn
    private String junk1;

    private String junk2;

    private BigDecimal total;
    private boolean paid;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public double getDiscount() {
        return discount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getJunk1() {
        return junk1;
    }

    public void setJunk1(String junk1) {
        this.junk1 = junk1;
    }

    // Test for a calculated field. Should be ignored by Persism because it's a read-only property.
    public String getJunk2() {
        if (junk2 == null) {
            junk2 = junk1 + " COW";
        }
        return junk2;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", customerId='" + customerId + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", discount=" + discount +
                ", total=" + total +
                ", paid=" + paid +
                '}';
    }
}
