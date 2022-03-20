package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotTable;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: danhoward
 * Date: 12-05-13
 * Time: 6:39 AM
 */
@NotTable
public final class CustomerOrder {

    @Column(primary = true) // makes no sense in a @View or @NotTable. We warn
    private String customerId;

    private String companyName;
    private String description;
    private long orderId;
    private Date dateCreated;
    private Date datePaid;
    private boolean paid;

    public CustomerOrder() {
    }

    public CustomerOrder(String customerId, String companyName, String description, long orderId, Date dateCreated, Date datePaid, boolean paid) {
        this.customerId = customerId;
        this.companyName = companyName;
        this.description = description;
        this.orderId = orderId;
        this.dateCreated = dateCreated;
        this.datePaid = datePaid;
        this.paid = paid;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDescription() {
        return description;
    }

    public long getOrderId() {
        return orderId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public boolean isPaid() {
        return paid;
    }



    @Override
    public String toString() {
        return "CustomerOrder{" +
                "customerId='" + customerId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", description='" + description + '\'' +
                ", orderId=" + orderId +
                ", dateCreated=" + dateCreated +
                ", datePaid=" + datePaid +
                ", paid=" + paid +
                '}';
    }
}
