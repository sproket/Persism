package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;

public final class SupplierTransaction {
    private Integer supplierTransactionId;
    private Integer supplierId;
    private Integer transactionTypeId;
    private Integer purchaseOrderId;
    private Integer paymentMethodId;
    private String supplierInvoiceNumber;
    private Date transactionDate;
    private BigDecimal amountExcludingTax;
    private BigDecimal taxAmount;
    private BigDecimal transactionAmount;
    private BigDecimal outstandingBalance;
    private Date finalizationDate;
    private Boolean isFinalized;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getSupplierTransactionId() {
        return supplierTransactionId;
    }

    public void setSupplierTransactionId(Integer supplierTransactionId) {
        this.supplierTransactionId = supplierTransactionId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(Integer transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getSupplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) {
        this.supplierInvoiceNumber = supplierInvoiceNumber;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmountExcludingTax() {
        return amountExcludingTax;
    }

    public void setAmountExcludingTax(BigDecimal amountExcludingTax) {
        this.amountExcludingTax = amountExcludingTax;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public Date getFinalizationDate() {
        return finalizationDate;
    }

    public void setFinalizationDate(Date finalizationDate) {
        this.finalizationDate = finalizationDate;
    }

    public Boolean getFinalized() {
        return isFinalized;
    }

    public void setFinalized(Boolean finalized) {
        isFinalized = finalized;
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

        SupplierTransaction that = (SupplierTransaction) o;

        if (supplierTransactionId != null ? !supplierTransactionId.equals(that.supplierTransactionId) : that.supplierTransactionId != null) {
            return false;
        }
        if (supplierId != null ? !supplierId.equals(that.supplierId) : that.supplierId != null) {
            return false;
        }
        if (transactionTypeId != null ? !transactionTypeId.equals(that.transactionTypeId) : that.transactionTypeId != null) {
            return false;
        }
        if (purchaseOrderId != null ? !purchaseOrderId.equals(that.purchaseOrderId) : that.purchaseOrderId != null) {
            return false;
        }
        if (paymentMethodId != null ? !paymentMethodId.equals(that.paymentMethodId) : that.paymentMethodId != null) {
            return false;
        }
        if (supplierInvoiceNumber != null ? !supplierInvoiceNumber.equals(that.supplierInvoiceNumber) : that.supplierInvoiceNumber != null) {
            return false;
        }
        if (transactionDate != null ? !transactionDate.equals(that.transactionDate) : that.transactionDate != null) {
            return false;
        }
        if (amountExcludingTax != null ? !amountExcludingTax.equals(that.amountExcludingTax) : that.amountExcludingTax != null) {
            return false;
        }
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null) {
            return false;
        }
        if (transactionAmount != null ? !transactionAmount.equals(that.transactionAmount) : that.transactionAmount != null) {
            return false;
        }
        if (outstandingBalance != null ? !outstandingBalance.equals(that.outstandingBalance) : that.outstandingBalance != null) {
            return false;
        }
        if (finalizationDate != null ? !finalizationDate.equals(that.finalizationDate) : that.finalizationDate != null) {
            return false;
        }
        if (isFinalized != null ? !isFinalized.equals(that.isFinalized) : that.isFinalized != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = supplierTransactionId != null ? supplierTransactionId.hashCode() : 0;
        result = 31 * result + (supplierId != null ? supplierId.hashCode() : 0);
        result = 31 * result + (transactionTypeId != null ? transactionTypeId.hashCode() : 0);
        result = 31 * result + (purchaseOrderId != null ? purchaseOrderId.hashCode() : 0);
        result = 31 * result + (paymentMethodId != null ? paymentMethodId.hashCode() : 0);
        result = 31 * result + (supplierInvoiceNumber != null ? supplierInvoiceNumber.hashCode() : 0);
        result = 31 * result + (transactionDate != null ? transactionDate.hashCode() : 0);
        result = 31 * result + (amountExcludingTax != null ? amountExcludingTax.hashCode() : 0);
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (transactionAmount != null ? transactionAmount.hashCode() : 0);
        result = 31 * result + (outstandingBalance != null ? outstandingBalance.hashCode() : 0);
        result = 31 * result + (finalizationDate != null ? finalizationDate.hashCode() : 0);
        result = 31 * result + (isFinalized != null ? isFinalized.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SupplierTransaction{" +
               "supplierTransactionId=" + supplierTransactionId +
               ", supplierId=" + supplierId +
               ", transactionTypeId=" + transactionTypeId +
               ", purchaseOrderId=" + purchaseOrderId +
               ", paymentMethodId=" + paymentMethodId +
               ", supplierInvoiceNumber='" + supplierInvoiceNumber + '\'' +
               ", transactionDate=" + transactionDate +
               ", amountExcludingTax=" + amountExcludingTax +
               ", taxAmount=" + taxAmount +
               ", transactionAmount=" + transactionAmount +
               ", outstandingBalance=" + outstandingBalance +
               ", finalizationDate=" + finalizationDate +
               ", isFinalized=" + isFinalized +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
