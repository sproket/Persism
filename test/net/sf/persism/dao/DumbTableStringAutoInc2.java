package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

@Table("DumbTableStringAutoInc")
public final class DumbTableStringAutoInc2 {
    @Column(autoIncrement = true, primary = true)
    private String id;

    public String getId() {
        return id;
    }

    // no setter

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
