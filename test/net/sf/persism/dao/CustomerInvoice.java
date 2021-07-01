package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.View;

import java.util.Date;

/**
 * Test for View where Persism finds it by name.
 */
@View()
public final class CustomerInvoice {
    private String customerId;

    private String companyName;
    private long invoiceId;
    private Date dateCreated;
    private boolean paid;
    private short status;
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

    public short getStatus() {
        return status;
    }

    public int getQuantity() {
        return quantity;
    }

    // included for setter warning.
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "\nCustomerInvoice{" +
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
