package net.sf.persism.dao.records;

import net.sf.persism.InitializeEvent;
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
        int quantity) implements InitializeEvent {

    private static boolean initialized = false;

    @Override
    public void onInitialized() {
        System.out.println("CustomerInvoiceRec initialized " + this);
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
