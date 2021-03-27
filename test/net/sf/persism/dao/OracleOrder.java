package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Oracle test for generating auto-inc. Oracle does not detect auto inc so you have to spell it out...
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/21/11
 * Time: 2:30 PM
 */
@Table(value = "ORDERS")
public final class OracleOrder extends Order {

    @Override
    public long getId() {
        return super.getId();
    }

    private BigDecimal bit1;

    private String bit2;


    // Adding useless annotations on the field, the getter and the setter to detect for testing.

    @NotColumn
    private int junk1; // annotation on field
    private int junk2; // annotation on getter
    private int junk3; // annotation on setter

    public int getJunk1() {
        return junk1;
    }

    public void setJunk1(int junk1) {
        this.junk1 = junk1;
    }

    @NotColumn
    public int getJunk2() {
        return junk2;
    }

    public void setJunk2(int junk2) {
        this.junk2 = junk2;
    }

    public int getJunk3() {
        return junk3;
    }

    @NotColumn
    public void setJunk3(int junk3) {
        this.junk3 = junk3;
    }

    public BigDecimal getBit1() {
        return bit1;
    }

    public void setBit1(BigDecimal bit1) {
        this.bit1 = bit1;
    }

    public String getBit2() {
        return bit2;
    }

    public void setBit2(String bit2) {
        this.bit2 = bit2;
    }

    // NO SETTER
    @Override
    public LocalDate getCreated() {
        return super.getCreated();
    }

    @Override
    public String toString() {

        return "Order{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", created=" + getCreated() +
                ", customerId='" + getCustomerId() + '\'' +
                ", paid=" + isPaid() +
                ", bit1=" + bit1 + '\'' +
                ", bit2='" + bit2 + '\'' +
                '}';

    }
}
