package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

import java.math.BigInteger;
import java.util.Date;

public final class OracleBit {

    @Column(primary = true)
    private long id;

    private String name;
    private Date created;
    private String customerId;
    private Boolean paid;
    private Boolean garbage;
    private BigInteger biggie;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
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

    public Boolean isGarbage() {
        return garbage;
    }

    public void setGarbage(Boolean garbage) {
        this.garbage = garbage;
    }

    public BigInteger getBiggie() {
        return biggie;
    }

    public void setBiggie(BigInteger biggie) {
        this.biggie = biggie;
    }

    @Override
    public String toString() {
        return "OracleBit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", customerId='" + customerId + '\'' +
                ", paid=" + paid +
                ", garbage=" + garbage +
                '}';
    }
}
