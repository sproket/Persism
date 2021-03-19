package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;

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
    //@Column(autoIncrement = true)
    public long getId() {
        return super.getId();
    }

    public BigDecimal bit1;
    public String bit2;

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
