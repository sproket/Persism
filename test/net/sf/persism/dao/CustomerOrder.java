package net.sf.persism.dao;

import net.sf.persism.annotations.Query;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: danhoward
 * Date: 12-05-13
 * Time: 6:39 AM
 */
@Query
public class CustomerOrder {

    private String customerId;
    private String companyName;
    private String description;
    private long orderId;
    private Date dateCreated;
    private boolean paid;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "\nCustomerOrder{" +
                "customerId='" + customerId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", description='" + description + '\'' +
                ", orderId=" + orderId +
                ", dateCreated=" + dateCreated +
                ", paid=" + paid +
                '}';
    }
}
