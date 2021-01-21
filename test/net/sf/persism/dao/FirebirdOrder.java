package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

@Table(value = "ORDERS")
public class FirebirdOrder extends Order {
    // firebird can't seem to detect auto incs. CLOWNS
    @Override
    @Column(autoIncrement = true)
    public long getId() {
        return super.getId();
    }

}
