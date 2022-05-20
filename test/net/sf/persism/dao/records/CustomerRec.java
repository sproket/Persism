package net.sf.persism.dao.records;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.Table;
import net.sf.persism.dao.Invoice;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

@Table("Customers")
public record CustomerRec(

        String customerId,

        String companyName,

        String contactName,

        Character status,

        @Join(to = Invoice.class, onProperties = "customerId", toProperties = "customerId")
        List<Invoice> invoices) {

    // JOIN can only work if we make a constructor without the Join and make it a modifiable list.
    @ConstructorProperties({"status", "customerId", "companyName", "contactName"})
    public CustomerRec(Character status, String customerId, String companyName, String contactName) {
        this(customerId, companyName, contactName, status, new ArrayList<>());
    }
}
