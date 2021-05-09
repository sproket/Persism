package net.sf.persism.dao.records;

import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;
import net.sf.persism.dao.Invoice;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("Invoices")
public record InvoiceRec(Integer invoiceId, String customerId, float price, int quantity, double discount,
                         BigDecimal actualPrice, LocalDateTime created, boolean paid, @NotColumn String junk) {

    public InvoiceRec {
        System.out.println("INVOICE SELECTED CONSTRUCTOR " + junk);
    }

    @ConstructorProperties({"paid", "invoiceId", "customerId", "price", "quantity", "discount", "actualPrice", "created"})
    public InvoiceRec(boolean paid, Integer invoiceId, String customerId, float price, int quantity, double discount,
                      BigDecimal actualPrice, LocalDateTime created) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid, "different order 3");
    }

    public InvoiceRec(boolean paid, Integer invoiceId,  float price, String customerId, int quantity, double discount,
                      BigDecimal actualPrice, LocalDateTime created) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid, "different order");
    }

    public InvoiceRec(boolean paid, Integer invoiceId,  float price, String customerId, double discount, int quantity,
                      BigDecimal actualPrice, LocalDateTime created) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid, "all different order");
    }

    public InvoiceRec(boolean paid, Integer invoiceId,  float price, String customerId, double discount, int quantity,
                       LocalDateTime created, BigDecimal actualPrice) {
        this(invoiceId, customerId, price, quantity, discount, actualPrice, created, paid, "all different order 2");
    }


    public InvoiceRec(String customerId, float price, int quantity, double discount, BigDecimal actualPrice, boolean paid) {
        this(null, customerId, price, quantity, discount, actualPrice, null, paid, "missing invoice id & actualPrice");
    }

    public InvoiceRec(Invoice invoice) {
        this(invoice.getInvoiceId(), invoice.getCustomerId(), invoice.getPrice(), invoice.getQuantity(),
                invoice.getDiscount(), invoice.getActualPrice(), invoice.getCreated(), invoice.isPaid(), "copy constructor");
    }

    // should fail unless @NotColumn? - nope. not required. We look at fields, not methods
    // @NotColumn
    public BigDecimal total() {
        return new BigDecimal(price * quantity);
    }

}
