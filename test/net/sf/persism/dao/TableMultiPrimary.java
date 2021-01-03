package net.sf.persism.dao;

import java.util.Date;

/**
 * Comments for TableMultiPrimary go here.
 *
 * @author danhoward
 * @since 12-05-21 6:19 AM
 */
public class TableMultiPrimary {

    private int id;

    private String customerName;

    private String field4;

    private Date field5;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
        return "TableMultiPrimary{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", field4='" + field4 + '\'' +
                ", field5=" + field5 +
                '}';
    }
}
