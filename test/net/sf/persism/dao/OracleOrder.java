package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.TableName;

/**
 * Oracle test for generating auto-inc. Oracle does not detect auto inc so you have to spell it out...
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/21/11
 * Time: 2:30 PM
 */
@TableName("ORDERS")
public final class OracleOrder extends Order {

    @Override
    @Column(autoIncrement = true)
    public long getId() {
        return super.getId();
    }

}
