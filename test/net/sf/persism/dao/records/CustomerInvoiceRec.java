package net.sf.persism.dao.records;

import net.sf.persism.annotations.View;

import java.util.Date;

@View("CustomerInvoice")
public record CustomerInvoiceRec(
        String customerId,
        String companyName,
        long invoiceId,
        Date dateCreated,
        boolean paid,
        Character status,
        int quantity) {
}
