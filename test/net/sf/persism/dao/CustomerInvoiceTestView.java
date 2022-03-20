package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.View;

import java.util.Date;

/**
 * Test for vew with a non-matching name
 */
@View("CustomerInvoice")
public final class CustomerInvoiceTestView {

    @Column(primary = true)
    private String customerId;

    private String companyName;
    private long invoiceId;
    private Date dateCreated;
    private boolean paid;
    private Character status;
    private int quantity;

    public String getCustomerId() {
        return customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public boolean isPaid() {
        return paid;
    }

    public Character getStatus() {
        return status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "\nCustomerInvoiceTestView{" +
                "customerId='" + customerId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", invoiceId=" + invoiceId +
                ", dateCreated=" + dateCreated +
                ", paid=" + paid +
                ", status=" + status +
                ", quantity=" + quantity +
                '}';
    }
}
