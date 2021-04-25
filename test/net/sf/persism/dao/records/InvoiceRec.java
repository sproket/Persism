package net.sf.persism.dao.records;

import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;
import net.sf.persism.dao.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("Invoices")
public record InvoiceRec(Integer invoiceId, String customerId, float price, int quantity, double discount,
                         BigDecimal actualPrice, LocalDateTime created, boolean paid) {

    private static int junk;

    public InvoiceRec(boolean paid, Integer invoiceId, String customerId, float price, int quantity, double discount,
                      BigDecimal actualPrice, LocalDateTime created) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid);
    }

    public InvoiceRec(boolean paid, Integer invoiceId,  float price, String customerId, int quantity, double discount,
                      BigDecimal actualPrice, LocalDateTime created) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid);
    }

    public InvoiceRec(boolean paid, Integer invoiceId,  float price, String customerId, double discount, int quantity,
                      BigDecimal actualPrice, LocalDateTime created) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid);
    }

    public InvoiceRec(boolean paid, Integer invoiceId,  float price, String customerId, double discount, int quantity,
                       LocalDateTime created, BigDecimal actualPrice) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid);
    }


    public InvoiceRec(boolean paid, Integer invoiceId, String customerId, float price, int quantity, double discount,
                      BigDecimal actualPrice, int junk) { // LocalDateTime created,
        this(invoiceId, customerId, price, quantity, discount, actualPrice, null, paid);
        this.junk = junk;
    }

    public InvoiceRec(String customerId, float price, int quantity, double discount, BigDecimal actualPrice, boolean paid) {
        this(null, customerId, price, quantity, discount, actualPrice, null, paid);
    }

    public InvoiceRec(Invoice invoice) {
        this(invoice.getInvoiceId(), invoice.getCustomerId(), invoice.getPrice(), invoice.getQuantity(),
                invoice.getDiscount(), invoice.getActualPrice(), invoice.getCreated(), invoice.isPaid());
    }

    // should fail unless @NotColumn?
    @NotColumn
    public BigDecimal total() {
        return new BigDecimal(price * quantity);
    }

}
