package net.sf.persism.dao;

import java.time.LocalDate;

public class CorporateHoliday {
    private String id;
    private String name;
    private LocalDate date;

    public CorporateHoliday() {
    }

    public CorporateHoliday(String id, String name, LocalDate date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
