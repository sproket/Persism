package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public final class Product {

    @Column(primary = true)
    private int id;

    private String description;
    private double cost;
    private BigDecimal badNumber;
    private Date badDate;
    private Timestamp badTimestamp;

    public Product() {
    }

    public Product(int id, String description, double cost) {
        this.id = id;
        this.description = description;
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public BigDecimal getBadNumber() {
        return badNumber;
    }

    public void setBadNumber(BigDecimal badNumber) {
        this.badNumber = badNumber;
    }

    public Date getBadDate() {
        return badDate;
    }

    public void setBadDate(Date badDate) {
        this.badDate = badDate;
    }

    public Timestamp getBadTimestamp() {
        return badTimestamp;
    }

    public void setBadTimestamp(Timestamp badTimestamp) {
        this.badTimestamp = badTimestamp;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                '}';
    }
}

