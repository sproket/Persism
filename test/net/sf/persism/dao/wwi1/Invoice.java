package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;

// s
public final class Invoice {
    private Integer invoiceId;
    private Integer customerId;
    private Integer billToCustomerId;
    private Integer orderId;
    private Integer deliveryMethodId;
    private Integer contactPersonId;
    private Integer accountsPersonId;
    private Integer salespersonPersonId;
    private Integer packedByPersonId;
    private Date invoiceDate;
    private String customerPurchaseOrderNumber;
    private Boolean isCreditNote;
    private String creditNoteReason;
    private String comments;
    private String deliveryInstructions;
    private String internalComments;
    private Integer totalDryItems;
    private Integer totalChillerItems;
    private String deliveryRun;
    private String runPosition;
    private String returnedDeliveryData;
    private Date confirmedDeliveryTime;
    private String confirmedReceivedBy;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer invoiceId() {
        return invoiceId;
    }

    public Invoice setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public Integer customerId() {
        return customerId;
    }

    public Invoice setCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public Integer billToCustomerId() {
        return billToCustomerId;
    }

    public Invoice setBillToCustomerId(Integer billToCustomerId) {
        this.billToCustomerId = billToCustomerId;
        return this;
    }

    public Integer orderId() {
        return orderId;
    }

    public Invoice setOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public Integer deliveryMethodId() {
        return deliveryMethodId;
    }

    public Invoice setDeliveryMethodId(Integer deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
        return this;
    }

    public Integer contactPersonId() {
        return contactPersonId;
    }

    public Invoice setContactPersonId(Integer contactPersonId) {
        this.contactPersonId = contactPersonId;
        return this;
    }

    public Integer accountsPersonId() {
        return accountsPersonId;
    }

    public Invoice setAccountsPersonId(Integer accountsPersonId) {
        this.accountsPersonId = accountsPersonId;
        return this;
    }

    public Integer salespersonPersonId() {
        return salespersonPersonId;
    }

    public Invoice setSalespersonPersonId(Integer salespersonPersonId) {
        this.salespersonPersonId = salespersonPersonId;
        return this;
    }

    public Integer packedByPersonId() {
        return packedByPersonId;
    }

    public Invoice setPackedByPersonId(Integer packedByPersonId) {
        this.packedByPersonId = packedByPersonId;
        return this;
    }

    public Date invoiceDate() {
        return invoiceDate;
    }

    public Invoice setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public String customerPurchaseOrderNumber() {
        return customerPurchaseOrderNumber;
    }

    public Invoice setCustomerPurchaseOrderNumber(String customerPurchaseOrderNumber) {
        this.customerPurchaseOrderNumber = customerPurchaseOrderNumber;
        return this;
    }

    public Boolean isCreditNote() {
        return isCreditNote;
    }

    public Invoice setCreditNote(Boolean creditNote) {
        isCreditNote = creditNote;
        return this;
    }

    public String creditNoteReason() {
        return creditNoteReason;
    }

    public Invoice setCreditNoteReason(String creditNoteReason) {
        this.creditNoteReason = creditNoteReason;
        return this;
    }

    public String comments() {
        return comments;
    }

    public Invoice setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public String deliveryInstructions() {
        return deliveryInstructions;
    }

    public Invoice setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
        return this;
    }

    public String internalComments() {
        return internalComments;
    }

    public Invoice setInternalComments(String internalComments) {
        this.internalComments = internalComments;
        return this;
    }

    public Integer totalDryItems() {
        return totalDryItems;
    }

    public Invoice setTotalDryItems(Integer totalDryItems) {
        this.totalDryItems = totalDryItems;
        return this;
    }

    public Integer totalChillerItems() {
        return totalChillerItems;
    }

    public Invoice setTotalChillerItems(Integer totalChillerItems) {
        this.totalChillerItems = totalChillerItems;
        return this;
    }

    public String deliveryRun() {
        return deliveryRun;
    }

    public Invoice setDeliveryRun(String deliveryRun) {
        this.deliveryRun = deliveryRun;
        return this;
    }

    public String runPosition() {
        return runPosition;
    }

    public Invoice setRunPosition(String runPosition) {
        this.runPosition = runPosition;
        return this;
    }

    public String returnedDeliveryData() {
        return returnedDeliveryData;
    }

    public Invoice setReturnedDeliveryData(String returnedDeliveryData) {
        this.returnedDeliveryData = returnedDeliveryData;
        return this;
    }

    public Date confirmedDeliveryTime() {
        return confirmedDeliveryTime;
    }

    public Invoice setConfirmedDeliveryTime(Date confirmedDeliveryTime) {
        this.confirmedDeliveryTime = confirmedDeliveryTime;
        return this;
    }

    public String confirmedReceivedBy() {
        return confirmedReceivedBy;
    }

    public Invoice setConfirmedReceivedBy(String confirmedReceivedBy) {
        this.confirmedReceivedBy = confirmedReceivedBy;
        return this;
    }

    public Integer lastEditedBy() {
        return lastEditedBy;
    }

    public Invoice setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
        return this;
    }

    public Date lastEditedWhen() {
        return lastEditedWhen;
    }

    public Invoice setLastEditedWhen(Date lastEditedWhen) {
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

        Invoice invoice = (Invoice) o;

        if (invoiceId != null ? !invoiceId.equals(invoice.invoiceId) : invoice.invoiceId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(invoice.customerId) : invoice.customerId != null) {
            return false;
        }
        if (billToCustomerId != null ? !billToCustomerId.equals(invoice.billToCustomerId) : invoice.billToCustomerId != null) {
            return false;
        }
        if (orderId != null ? !orderId.equals(invoice.orderId) : invoice.orderId != null) {
            return false;
        }
        if (deliveryMethodId != null ? !deliveryMethodId.equals(invoice.deliveryMethodId) : invoice.deliveryMethodId != null) {
            return false;
        }
        if (contactPersonId != null ? !contactPersonId.equals(invoice.contactPersonId) : invoice.contactPersonId != null) {
            return false;
        }
        if (accountsPersonId != null ? !accountsPersonId.equals(invoice.accountsPersonId) : invoice.accountsPersonId != null) {
            return false;
        }
        if (salespersonPersonId != null ? !salespersonPersonId.equals(invoice.salespersonPersonId) : invoice.salespersonPersonId != null) {
            return false;
        }
        if (packedByPersonId != null ? !packedByPersonId.equals(invoice.packedByPersonId) : invoice.packedByPersonId != null) {
            return false;
        }
        if (invoiceDate != null ? !invoiceDate.equals(invoice.invoiceDate) : invoice.invoiceDate != null) {
            return false;
        }
        if (customerPurchaseOrderNumber != null ? !customerPurchaseOrderNumber.equals(invoice.customerPurchaseOrderNumber) : invoice.customerPurchaseOrderNumber != null) {
            return false;
        }
        if (isCreditNote != null ? !isCreditNote.equals(invoice.isCreditNote) : invoice.isCreditNote != null) {
            return false;
        }
        if (creditNoteReason != null ? !creditNoteReason.equals(invoice.creditNoteReason) : invoice.creditNoteReason != null) {
            return false;
        }
        if (comments != null ? !comments.equals(invoice.comments) : invoice.comments != null) {
            return false;
        }
        if (deliveryInstructions != null ? !deliveryInstructions.equals(invoice.deliveryInstructions) : invoice.deliveryInstructions != null) {
            return false;
        }
        if (internalComments != null ? !internalComments.equals(invoice.internalComments) : invoice.internalComments != null) {
            return false;
        }
        if (totalDryItems != null ? !totalDryItems.equals(invoice.totalDryItems) : invoice.totalDryItems != null) {
            return false;
        }
        if (totalChillerItems != null ? !totalChillerItems.equals(invoice.totalChillerItems) : invoice.totalChillerItems != null) {
            return false;
        }
        if (deliveryRun != null ? !deliveryRun.equals(invoice.deliveryRun) : invoice.deliveryRun != null) {
            return false;
        }
        if (runPosition != null ? !runPosition.equals(invoice.runPosition) : invoice.runPosition != null) {
            return false;
        }
        if (returnedDeliveryData != null ? !returnedDeliveryData.equals(invoice.returnedDeliveryData) : invoice.returnedDeliveryData != null) {
            return false;
        }
        if (confirmedDeliveryTime != null ? !confirmedDeliveryTime.equals(invoice.confirmedDeliveryTime) : invoice.confirmedDeliveryTime != null) {
            return false;
        }
        if (confirmedReceivedBy != null ? !confirmedReceivedBy.equals(invoice.confirmedReceivedBy) : invoice.confirmedReceivedBy != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(invoice.lastEditedBy) : invoice.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(invoice.lastEditedWhen) : invoice.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = invoiceId != null ? invoiceId.hashCode() : 0;
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (billToCustomerId != null ? billToCustomerId.hashCode() : 0);
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        result = 31 * result + (deliveryMethodId != null ? deliveryMethodId.hashCode() : 0);
        result = 31 * result + (contactPersonId != null ? contactPersonId.hashCode() : 0);
        result = 31 * result + (accountsPersonId != null ? accountsPersonId.hashCode() : 0);
        result = 31 * result + (salespersonPersonId != null ? salespersonPersonId.hashCode() : 0);
        result = 31 * result + (packedByPersonId != null ? packedByPersonId.hashCode() : 0);
        result = 31 * result + (invoiceDate != null ? invoiceDate.hashCode() : 0);
        result = 31 * result + (customerPurchaseOrderNumber != null ? customerPurchaseOrderNumber.hashCode() : 0);
        result = 31 * result + (isCreditNote != null ? isCreditNote.hashCode() : 0);
        result = 31 * result + (creditNoteReason != null ? creditNoteReason.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (deliveryInstructions != null ? deliveryInstructions.hashCode() : 0);
        result = 31 * result + (internalComments != null ? internalComments.hashCode() : 0);
        result = 31 * result + (totalDryItems != null ? totalDryItems.hashCode() : 0);
        result = 31 * result + (totalChillerItems != null ? totalChillerItems.hashCode() : 0);
        result = 31 * result + (deliveryRun != null ? deliveryRun.hashCode() : 0);
        result = 31 * result + (runPosition != null ? runPosition.hashCode() : 0);
        result = 31 * result + (returnedDeliveryData != null ? returnedDeliveryData.hashCode() : 0);
        result = 31 * result + (confirmedDeliveryTime != null ? confirmedDeliveryTime.hashCode() : 0);
        result = 31 * result + (confirmedReceivedBy != null ? confirmedReceivedBy.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Invoice{" +
               "invoiceId=" + invoiceId +
               ", customerId=" + customerId +
               ", billToCustomerId=" + billToCustomerId +
               ", orderId=" + orderId +
               ", deliveryMethodId=" + deliveryMethodId +
               ", contactPersonId=" + contactPersonId +
               ", accountsPersonId=" + accountsPersonId +
               ", salespersonPersonId=" + salespersonPersonId +
               ", packedByPersonId=" + packedByPersonId +
               ", invoiceDate=" + invoiceDate +
               ", customerPurchaseOrderNumber='" + customerPurchaseOrderNumber + '\'' +
               ", isCreditNote=" + isCreditNote +
               ", creditNoteReason='" + creditNoteReason + '\'' +
               ", comments='" + comments + '\'' +
               ", deliveryInstructions='" + deliveryInstructions + '\'' +
               ", internalComments='" + internalComments + '\'' +
               ", totalDryItems=" + totalDryItems +
               ", totalChillerItems=" + totalChillerItems +
               ", deliveryRun='" + deliveryRun + '\'' +
               ", runPosition='" + runPosition + '\'' +
               ", returnedDeliveryData='" + returnedDeliveryData + '\'' +
               ", confirmedDeliveryTime=" + confirmedDeliveryTime +
               ", confirmedReceivedBy='" + confirmedReceivedBy + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
