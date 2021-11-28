package net.sf.persism.dao;

import net.sf.persism.annotations.NotTable;

import java.util.Date;

/**
 * Test for class returning from a call to a view
 */
@NotTable
public final class CustomerInvoiceResult {
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

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "\nCustomerInvoiceResult{" +
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
