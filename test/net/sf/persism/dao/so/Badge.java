package net.sf.persism.dao.so;

import java.sql.Timestamp;

public final class Badge {
    private Integer id;
    private String name;
    private Integer userId;
    private Timestamp date;

    public Badge() {
    }

    public Badge(Integer id, String name, Integer userId, Timestamp date) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Badge badges = (Badge) o;

        if (id != null ? !id.equals(badges.id) : badges.id != null) {
            return false;
        }
        if (name != null ? !name.equals(badges.name) : badges.name != null) {
            return false;
        }
        if (userId != null ? !userId.equals(badges.userId) : badges.userId != null) {
            return false;
        }
        if (date != null ? !date.equals(badges.date) : badges.date != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
