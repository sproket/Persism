package net.sf.persism.dao.wwi1;

import java.sql.Date;

// p
public final class PurchaseOrder {
    private Integer purchaseOrderId;
    private Integer supplierId;
    private Date orderDate;
    private Integer deliveryMethodId;
    private Integer contactPersonId;
    private Date expectedDeliveryDate;
    private String supplierReference;
    private Boolean isOrderFinalized;
    private String comments;
    private String internalComments;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Integer getDeliveryMethodId() {
        return deliveryMethodId;
    }

    public void setDeliveryMethodId(Integer deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
    }

    public Integer getContactPersonId() {
        return contactPersonId;
    }

    public void setContactPersonId(Integer contactPersonId) {
        this.contactPersonId = contactPersonId;
    }

    public Date getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getSupplierReference() {
        return supplierReference;
    }

    public void setSupplierReference(String supplierReference) {
        this.supplierReference = supplierReference;
    }

    public Boolean getOrderFinalized() {
        return isOrderFinalized;
    }

    public void setOrderFinalized(Boolean orderFinalized) {
        isOrderFinalized = orderFinalized;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getInternalComments() {
        return internalComments;
    }

    public void setInternalComments(String internalComments) {
        this.internalComments = internalComments;
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

        PurchaseOrder that = (PurchaseOrder) o;

        if (purchaseOrderId != null ? !purchaseOrderId.equals(that.purchaseOrderId) : that.purchaseOrderId != null) {
            return false;
        }
        if (supplierId != null ? !supplierId.equals(that.supplierId) : that.supplierId != null) {
            return false;
        }
        if (orderDate != null ? !orderDate.equals(that.orderDate) : that.orderDate != null) {
            return false;
        }
        if (deliveryMethodId != null ? !deliveryMethodId.equals(that.deliveryMethodId) : that.deliveryMethodId != null) {
            return false;
        }
        if (contactPersonId != null ? !contactPersonId.equals(that.contactPersonId) : that.contactPersonId != null) {
            return false;
        }
        if (expectedDeliveryDate != null ? !expectedDeliveryDate.equals(that.expectedDeliveryDate) : that.expectedDeliveryDate != null) {
            return false;
        }
        if (supplierReference != null ? !supplierReference.equals(that.supplierReference) : that.supplierReference != null) {
            return false;
        }
        if (isOrderFinalized != null ? !isOrderFinalized.equals(that.isOrderFinalized) : that.isOrderFinalized != null) {
            return false;
        }
        if (comments != null ? !comments.equals(that.comments) : that.comments != null) {
            return false;
        }
        if (internalComments != null ? !internalComments.equals(that.internalComments) : that.internalComments != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = purchaseOrderId != null ? purchaseOrderId.hashCode() : 0;
        result = 31 * result + (supplierId != null ? supplierId.hashCode() : 0);
        result = 31 * result + (orderDate != null ? orderDate.hashCode() : 0);
        result = 31 * result + (deliveryMethodId != null ? deliveryMethodId.hashCode() : 0);
        result = 31 * result + (contactPersonId != null ? contactPersonId.hashCode() : 0);
        result = 31 * result + (expectedDeliveryDate != null ? expectedDeliveryDate.hashCode() : 0);
        result = 31 * result + (supplierReference != null ? supplierReference.hashCode() : 0);
        result = 31 * result + (isOrderFinalized != null ? isOrderFinalized.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (internalComments != null ? internalComments.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
               "purchaseOrderId=" + purchaseOrderId +
               ", supplierId=" + supplierId +
               ", orderDate=" + orderDate +
               ", deliveryMethodId=" + deliveryMethodId +
               ", contactPersonId=" + contactPersonId +
               ", expectedDeliveryDate=" + expectedDeliveryDate +
               ", supplierReference='" + supplierReference + '\'' +
               ", isOrderFinalized=" + isOrderFinalized +
               ", comments='" + comments + '\'' +
               ", internalComments='" + internalComments + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
