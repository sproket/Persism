package net.sf.persism.dao;

import net.sf.persism.annotations.Join;

public final class InvoiceLineItem {
    private int id;
    private int invoiceId;
    private int productId;
    private int quantity;

    @Join(to = Product.class, onProperties = "productId", toProperties = "id")
    private Product product;

    public InvoiceLineItem() {
    }

    public InvoiceLineItem(int invoiceId, int productId, int quantity) {
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

//    public void setProduct(Product product) {
//        this.product = product;
//    }

    @Override
    public String toString() {
        return "InvoiceLineItem{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", product=" + product +
                '}';
    }
}
