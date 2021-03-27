package net.sf.persism.dao;

import net.sf.persism.PersistableObject;
import net.sf.persism.annotations.Column;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Example of extending PersistableObject without specifying the generic type.
 * Works but IntelliJ warns you about it.
 * The usual way would be: class Order extends PersistableObject<Order>
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/21/11
 * Time: 2:30 PM
 */
public class Order extends PersistableObject {

    private long id;
    private String name;
    private LocalDate created;
    private String customerId;
    private LocalDateTime datePaid;
    private Timestamp dateSomething;

    private Boolean paid; // this has an Is and a Get method
    private boolean prepaid; // should map to Prepaid
    private boolean isCollect; // should map to IsCollect - IS
    private boolean isCancelled; // Should map to IsCancelled

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Boolean isPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Boolean getPaid() {
        return paid;
    }

    public boolean isPrepaid() {
        return prepaid;
    }

    public void setPrepaid(boolean prepaid) {
        this.prepaid = prepaid;
    }

    public boolean isCollect() {
        return isCollect;
    }

    public void setCollect(boolean collect) {
        isCollect = collect;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public LocalDateTime getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(LocalDateTime datePaid) {
        this.datePaid = datePaid;
    }

    public Timestamp getDateSomething() {
        return dateSomething;
    }

    public void setDateSomething(Timestamp dateSomething) {
        this.dateSomething = dateSomething;
    }


    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", customerId='" + customerId + '\'' +
                ", datePaid=" + datePaid +
                ", dateSomething=" + dateSomething +
                ", paid=" + paid +
                ", prepaid=" + prepaid +
                ", isCollect=" + isCollect +
                ", isCancelled=" + isCancelled +
                '}';
    }
}
