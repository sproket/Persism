package net.sf.persism.dao.wwi1;

import java.sql.Date;

// s
public final class Order {
    private Integer orderId;
    private Integer customerId;
    private Integer salespersonPersonId;
    private Integer pickedByPersonId;
    private Integer contactPersonId;
    private Integer backorderOrderId;
    private Date orderDate;
    private Date expectedDeliveryDate;
    private String customerPurchaseOrderNumber;
    private Boolean isUndersupplyBackordered;
    private String comments;
    private String deliveryInstructions;
    private String internalComments;
    private Date pickingCompletedWhen;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getSalespersonPersonId() {
        return salespersonPersonId;
    }

    public void setSalespersonPersonId(Integer salespersonPersonId) {
        this.salespersonPersonId = salespersonPersonId;
    }

    public Integer getPickedByPersonId() {
        return pickedByPersonId;
    }

    public void setPickedByPersonId(Integer pickedByPersonId) {
        this.pickedByPersonId = pickedByPersonId;
    }

    public Integer getContactPersonId() {
        return contactPersonId;
    }

    public void setContactPersonId(Integer contactPersonId) {
        this.contactPersonId = contactPersonId;
    }

    public Integer getBackorderOrderId() {
        return backorderOrderId;
    }

    public void setBackorderOrderId(Integer backorderOrderId) {
        this.backorderOrderId = backorderOrderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getCustomerPurchaseOrderNumber() {
        return customerPurchaseOrderNumber;
    }

    public void setCustomerPurchaseOrderNumber(String customerPurchaseOrderNumber) {
        this.customerPurchaseOrderNumber = customerPurchaseOrderNumber;
    }

    public Boolean getUndersupplyBackordered() {
        return isUndersupplyBackordered;
    }

    public void setUndersupplyBackordered(Boolean undersupplyBackordered) {
        isUndersupplyBackordered = undersupplyBackordered;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public String getInternalComments() {
        return internalComments;
    }

    public void setInternalComments(String internalComments) {
        this.internalComments = internalComments;
    }

    public Date getPickingCompletedWhen() {
        return pickingCompletedWhen;
    }

    public void setPickingCompletedWhen(Date pickingCompletedWhen) {
        this.pickingCompletedWhen = pickingCompletedWhen;
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

        Order order = (Order) o;

        if (orderId != null ? !orderId.equals(order.orderId) : order.orderId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(order.customerId) : order.customerId != null) {
            return false;
        }
        if (salespersonPersonId != null ? !salespersonPersonId.equals(order.salespersonPersonId) : order.salespersonPersonId != null) {
            return false;
        }
        if (pickedByPersonId != null ? !pickedByPersonId.equals(order.pickedByPersonId) : order.pickedByPersonId != null) {
            return false;
        }
        if (contactPersonId != null ? !contactPersonId.equals(order.contactPersonId) : order.contactPersonId != null) {
            return false;
        }
        if (backorderOrderId != null ? !backorderOrderId.equals(order.backorderOrderId) : order.backorderOrderId != null) {
            return false;
        }
        if (orderDate != null ? !orderDate.equals(order.orderDate) : order.orderDate != null) {
            return false;
        }
        if (expectedDeliveryDate != null ? !expectedDeliveryDate.equals(order.expectedDeliveryDate) : order.expectedDeliveryDate != null) {
            return false;
        }
        if (customerPurchaseOrderNumber != null ? !customerPurchaseOrderNumber.equals(order.customerPurchaseOrderNumber) : order.customerPurchaseOrderNumber != null) {
            return false;
        }
        if (isUndersupplyBackordered != null ? !isUndersupplyBackordered.equals(order.isUndersupplyBackordered) : order.isUndersupplyBackordered != null) {
            return false;
        }
        if (comments != null ? !comments.equals(order.comments) : order.comments != null) {
            return false;
        }
        if (deliveryInstructions != null ? !deliveryInstructions.equals(order.deliveryInstructions) : order.deliveryInstructions != null) {
            return false;
        }
        if (internalComments != null ? !internalComments.equals(order.internalComments) : order.internalComments != null) {
            return false;
        }
        if (pickingCompletedWhen != null ? !pickingCompletedWhen.equals(order.pickingCompletedWhen) : order.pickingCompletedWhen != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(order.lastEditedBy) : order.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(order.lastEditedWhen) : order.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (salespersonPersonId != null ? salespersonPersonId.hashCode() : 0);
        result = 31 * result + (pickedByPersonId != null ? pickedByPersonId.hashCode() : 0);
        result = 31 * result + (contactPersonId != null ? contactPersonId.hashCode() : 0);
        result = 31 * result + (backorderOrderId != null ? backorderOrderId.hashCode() : 0);
        result = 31 * result + (orderDate != null ? orderDate.hashCode() : 0);
        result = 31 * result + (expectedDeliveryDate != null ? expectedDeliveryDate.hashCode() : 0);
        result = 31 * result + (customerPurchaseOrderNumber != null ? customerPurchaseOrderNumber.hashCode() : 0);
        result = 31 * result + (isUndersupplyBackordered != null ? isUndersupplyBackordered.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (deliveryInstructions != null ? deliveryInstructions.hashCode() : 0);
        result = 31 * result + (internalComments != null ? internalComments.hashCode() : 0);
        result = 31 * result + (pickingCompletedWhen != null ? pickingCompletedWhen.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
               "orderId=" + orderId +
               ", customerId=" + customerId +
               ", salespersonPersonId=" + salespersonPersonId +
               ", pickedByPersonId=" + pickedByPersonId +
               ", contactPersonId=" + contactPersonId +
               ", backorderOrderId=" + backorderOrderId +
               ", orderDate=" + orderDate +
               ", expectedDeliveryDate=" + expectedDeliveryDate +
               ", customerPurchaseOrderNumber='" + customerPurchaseOrderNumber + '\'' +
               ", isUndersupplyBackordered=" + isUndersupplyBackordered +
               ", comments='" + comments + '\'' +
               ", deliveryInstructions='" + deliveryInstructions + '\'' +
               ", internalComments='" + internalComments + '\'' +
               ", pickingCompletedWhen=" + pickingCompletedWhen +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
