package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotColumn;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/*
util.Date (dt)
sql.Date (d)
sql.Time (t)
sql.Timestamp (dt)
LocalDate (d)
LocalTime (t)
LocalDateTime(dt)
Instant (dt)

SQL Date Types
Date
DateTime (Timestamp)
Time
*/

public final class DateTestLocalTypes {
    @Column(primary = true)
    private int id;

    private String description;

    private LocalDate dateOnly;
    private LocalTime timeOnly;
    private LocalDateTime dateAndTime;

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

    public LocalDate getDateOnly() {
        return dateOnly;
    }

    public void setDateOnly(LocalDate dateOnly) {
        this.dateOnly = dateOnly;
    }

    public LocalTime getTimeOnly() {
        return timeOnly;
    }

    public void setTimeOnly(LocalTime timeOnly) {
        this.timeOnly = timeOnly;
    }

    public LocalDateTime getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(LocalDateTime dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    @Override
    public String toString() {
        return "DateTestLocalTypes{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", dateOnly=" + dateOnly +
                ", timeOnly=" + timeOnly +
                ", dateAndTime=" + dateAndTime +
                '}';
    }
}
