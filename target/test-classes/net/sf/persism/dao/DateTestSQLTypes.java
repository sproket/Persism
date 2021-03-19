package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public final class DateTestSQLTypes {

    @Column(primary = true)
    private int id;

    private String description;

    private Date dateOnly;
    private Time timeOnly;
    private Timestamp dateAndTime;
    private java.util.Date utilDateAndTime;

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

    public Date getDateOnly() {
        return dateOnly;
    }

    public void setDateOnly(Date dateOnly) {
        this.dateOnly = dateOnly;
    }

    public Time getTimeOnly() {
        return timeOnly;
    }

    public void setTimeOnly(Time timeOnly) {
        this.timeOnly = timeOnly;
    }

    public Timestamp getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(Timestamp dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public java.util.Date getUtilDateAndTime() {
        return utilDateAndTime;
    }

    public void setUtilDateAndTime(java.util.Date utilDateAndTime) {
        this.utilDateAndTime = utilDateAndTime;
    }

    @Override
    public String toString() {
        return "DateTestSQLTypes{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", dateOnly=" + dateOnly +
                ", timeOnly=" + timeOnly +
                ", dateAndTime=" + dateAndTime +
                ", utilDateAndTime=" + utilDateAndTime +
                '}';
    }
}
