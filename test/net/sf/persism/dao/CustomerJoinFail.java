package net.sf.persism.dao;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.Table;

import java.util.HashSet;
import java.util.Set;

@Table("Customers")
public class CustomerJoinFail extends Customer {

    // todo 2 bad joins class mismatch
    // todo we can't do this for collections only for 1-1 joins

    @Join(to = InvoiceFail.class, onProperties = " CustomerId , sTatuS ", toProperties = "cusTomerId , status ")
    private Set<Invoice> invoices = new HashSet<>();

    @Join(to = InvoiceFail.class, onProperties = " CustomerId , sTatuS ", toProperties = "cusTomerId , status ")
    private Invoice whatever;

    @Override
    public Set<Invoice> getInvoices() {
        return invoices;
    }

    @Override
    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    @Override
    public Invoice getWhatever() {
        return whatever;
    }

    @Override
    public void setWhatever(Invoice whatever) {
        this.whatever = whatever;
    }
}
