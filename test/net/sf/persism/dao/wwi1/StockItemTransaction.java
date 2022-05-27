package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

// ware
public final class StockItemTransaction {
    private Integer stockItemTransactionId;
    private Integer stockItemId;
    private Integer transactionTypeId;
    private Integer customerId;
    private Integer invoiceId;
    private Integer supplierId;
    private Integer purchaseOrderId;
    private Date transactionOccurredWhen;
    private BigDecimal quantity;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getStockItemTransactionId() {
        return stockItemTransactionId;
    }

    public void setStockItemTransactionId(Integer stockItemTransactionId) {
        this.stockItemTransactionId = stockItemTransactionId;
    }

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public Integer getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(Integer transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Date getTransactionOccurredWhen() {
        return transactionOccurredWhen;
    }

    public void setTransactionOccurredWhen(Date transactionOccurredWhen) {
        this.transactionOccurredWhen = transactionOccurredWhen;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public Date getLastEditedWhen() {
        return lastEditedWhen;
    }

    public void setLastEditedWhen(Date lastEditedWhen) {
        this.lastEditedWhen = lastEditedWhen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StockItemTransaction that = (StockItemTransaction) o;

        if (stockItemTransactionId != null ? !stockItemTransactionId.equals(that.stockItemTransactionId) : that.stockItemTransactionId != null) {
            return false;
        }
        if (stockItemId != null ? !stockItemId.equals(that.stockItemId) : that.stockItemId != null) {
            return false;
        }
        if (transactionTypeId != null ? !transactionTypeId.equals(that.transactionTypeId) : that.transactionTypeId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) {
            return false;
        }
        if (invoiceId != null ? !invoiceId.equals(that.invoiceId) : that.invoiceId != null) {
            return false;
        }
        if (supplierId != null ? !supplierId.equals(that.supplierId) : that.supplierId != null) {
            return false;
        }
        if (purchaseOrderId != null ? !purchaseOrderId.equals(that.purchaseOrderId) : that.purchaseOrderId != null) {
            return false;
        }
        if (transactionOccurredWhen != null ? !transactionOccurredWhen.equals(that.transactionOccurredWhen) : that.transactionOccurredWhen != null) {
            return false;
        }
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = stockItemTransactionId != null ? stockItemTransactionId.hashCode() : 0;
        result = 31 * result + (stockItemId != null ? stockItemId.hashCode() : 0);
        result = 31 * result + (transactionTypeId != null ? transactionTypeId.hashCode() : 0);
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (supplierId != null ? supplierId.hashCode() : 0);
        result = 31 * result + (purchaseOrderId != null ? purchaseOrderId.hashCode() : 0);
        result = 31 * result + (transactionOccurredWhen != null ? transactionOccurredWhen.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockItemTransaction{" +
               "stockItemTransactionId=" + stockItemTransactionId +
               ", stockItemId=" + stockItemId +
               ", transactionTypeId=" + transactionTypeId +
               ", customerId=" + customerId +
               ", invoiceId=" + invoiceId +
               ", supplierId=" + supplierId +
               ", purchaseOrderId=" + purchaseOrderId +
               ", transactionOccurredWhen=" + transactionOccurredWhen +
               ", quantity=" + quantity +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
