package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

public final class Department {
    @Column(primary = true)
    private Integer id;

    private String name;
    private Boolean active;
    private char someType;

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

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public char getSomeType() {
        return someType;
    }

    public void setSomeType(char someType) {
        this.someType = someType;
    }
}
