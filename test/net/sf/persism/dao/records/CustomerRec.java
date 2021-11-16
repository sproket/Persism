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

    @ConstructorProperties({"customerId", "companyName", "contactName", "status"})
    public CustomerRec(String customerId, String companyName, String contactName, Character status) {
        this(customerId, companyName, contactName, status, new ArrayList<>());
    }
}
