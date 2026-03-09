package net.sf.persism.dao.records;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.Table;
import net.sf.persism.dao.Product;

import java.beans.ConstructorProperties;

/*
Currently, this will not work since we don't instantiate the Product POJO first.
 */
@Table("InvoiceLineItems")
public record InvoiceLineItemRec(

        int id,

        int invoiceId,

        int productId,

        int quantity,

        @Join(to = Product.class, onProperties = "productId", toProperties = "id")
        Product product) {

    @ConstructorProperties({"id", "invoiceId", "productId", "quantity"})
    public InvoiceLineItemRec(int id, int invoiceId, int productId, int quantity) {
        this(id, invoiceId, productId, quantity, null);
    }
}
