package net.sf.persism.dao;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("INVOICES")
public final class InvoiceFail {

    private Integer invoiceId;
    private String customerId;
    private float price;
    private int quantity;
    private double discount;
    private BigDecimal actualPrice;

    @NotColumn
    private String junk1;

    private LocalDateTime created;
    private boolean paid;

    // Used as a primitive to test for warning about using primitives on columns with defaults
    private Character status;

    @Join(to = InvoiceLineItem.class, onProperties = "invoiceId, price", toProperties = "invoiceId")
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

// remove setter for code coverage
//    public void setInvoiceId(Integer invoiceId) {
//        this.invoiceId = invoiceId;
//    }

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

    @NotColumn
    public BigDecimal getTotal() {
        return new BigDecimal(price * quantity);
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

    public List<InvoiceLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<InvoiceLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    // calculated property
    public String getJunk1() {
        return junk1 + " " + created;
    }


    // Read-only - generated by DB - NO Setter
    public LocalDateTime getCreated() {
        return created;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }


    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", customerId='" + customerId + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", discount=" + discount +
                ", actualPrice=" + actualPrice +
                ", junk1='" + junk1 + '\'' +
                ", created=" + created +
                ", total=" + getTotal() +
                ", paid=" + paid +
                ", status=" + status +
                "}\n";
    }
}
