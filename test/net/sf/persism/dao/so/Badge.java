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

    public Integer id() {
        return id;
    }

    public Badge id(Integer id) {
        this.id = id;
        return this;
    }

    public String name() {
        return name;
    }

    public Badge name(String name) {
        this.name = name;
        return this;
    }

    public Integer userId() {
        return userId;
    }

    public Badge userId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public Timestamp date() {
        return date;
    }

    public Badge date(Timestamp date) {
        this.date = date;
        return this;
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

    @Override
    public String toString() {
        return "Badge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                ", date=" + date +
                '}';
    }
}
