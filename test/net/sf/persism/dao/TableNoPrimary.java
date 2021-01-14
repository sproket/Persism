package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

import java.util.Date;

/**
 * TableNoPrimary class to demonstrate a table with no primary or auto inc defined.
 *
 * @author {danhoward}
 * @since 12-05-19 3:38 PM
 */
public class TableNoPrimary {

    //@Column(primary = true)
    private int id;

    private String name;

    private String field4;
    private Date field5;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public Date getField5() {
        return field5;
    }

    public void setField5(Date field5) {
        this.field5 = field5;
    }

    @Override
    public String toString() {
        return "TableNoPrimary{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", field4='" + field4 + '\'' +
                ", field5=" + field5 +
                '}';
    }
}

