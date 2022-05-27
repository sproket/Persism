package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;

// s
public final class CustomerTransaction {
    private Integer customerTransactionId;
    private Integer customerId;
    private Integer transactionTypeId;
    private Integer invoiceId;
    private Integer paymentMethodId;
    private Date transactionDate;
    private BigDecimal amountExcludingTax;
    private BigDecimal taxAmount;
    private BigDecimal transactionAmount;
    private BigDecimal outstandingBalance;
    private Date finalizationDate;
    private Boolean isFinalized;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer customerTransactionId() {
        return customerTransactionId;
    }

    public CustomerTransaction setCustomerTransactionId(Integer customerTransactionId) {
        this.customerTransactionId = customerTransactionId;
        return this;
    }

    public Integer customerId() {
        return customerId;
    }

    public CustomerTransaction setCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public Integer transactionTypeId() {
        return transactionTypeId;
    }

    public CustomerTransaction setTransactionTypeId(Integer transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
        return this;
    }

    public Integer invoiceId() {
        return invoiceId;
    }

    public CustomerTransaction setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public Integer paymentMethodId() {
        return paymentMethodId;
    }

    public CustomerTransaction setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
        return this;
    }

    public Date transactionDate() {
        return transactionDate;
    }

    public CustomerTransaction setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public BigDecimal amountExcludingTax() {
        return amountExcludingTax;
    }

    public CustomerTransaction setAmountExcludingTax(BigDecimal amountExcludingTax) {
        this.amountExcludingTax = amountExcludingTax;
        return this;
    }

    public BigDecimal taxAmount() {
        return taxAmount;
    }

    public CustomerTransaction setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        return this;
    }

    public BigDecimal transactionAmount() {
        return transactionAmount;
    }

    public CustomerTransaction setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
        return this;
    }

    public BigDecimal outstandingBalance() {
        return outstandingBalance;
    }

    public CustomerTransaction setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
        return this;
    }

    public Date finalizationDate() {
        return finalizationDate;
    }

    public CustomerTransaction setFinalizationDate(Date finalizationDate) {
        this.finalizationDate = finalizationDate;
        return this;
    }

    public Boolean isFinalized() {
        return isFinalized;
    }

    public CustomerTransaction setFinalized(Boolean finalized) {
        isFinalized = finalized;
        return this;
    }

    public Integer lastEditedBy() {
        return lastEditedBy;
    }

    public CustomerTransaction setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
        return this;
    }

    public Date lastEditedWhen() {
        return lastEditedWhen;
    }

    public CustomerTransaction setLastEditedWhen(Date lastEditedWhen) {
        this.lastEditedWhen = lastEditedWhen;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerTransaction that = (CustomerTransaction) o;

        if (customerTransactionId != null ? !customerTransactionId.equals(that.customerTransactionId) : that.customerTransactionId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) {
            return false;
        }
        if (transactionTypeId != null ? !transactionTypeId.equals(that.transactionTypeId) : that.transactionTypeId != null) {
            return false;
        }
        if (invoiceId != null ? !invoiceId.equals(that.invoiceId) : that.invoiceId != null) {
            return false;
        }
        if (paymentMethodId != null ? !paymentMethodId.equals(that.paymentMethodId) : that.paymentMethodId != null) {
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
        int result = customerTransactionId != null ? customerTransactionId.hashCode() : 0;
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (transactionTypeId != null ? transactionTypeId.hashCode() : 0);
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (paymentMethodId != null ? paymentMethodId.hashCode() : 0);
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
        return "CustomerTransaction{" +
               "customerTransactionId=" + customerTransactionId +
               ", customerId=" + customerId +
               ", transactionTypeId=" + transactionTypeId +
               ", invoiceId=" + invoiceId +
               ", paymentMethodId=" + paymentMethodId +
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
