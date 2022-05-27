package net.sf.persism.dao.wwi1;

import java.sql.Date;

public final class DeliveryMethod {
    private Integer deliveryMethodId;
    private String deliveryMethodName;
    private Integer lastEditedBy;

    public Integer getDeliveryMethodId() {
        return deliveryMethodId;
    }

    public void setDeliveryMethodId(Integer deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
    }

    public String getDeliveryMethodName() {
        return deliveryMethodName;
    }

    public void setDeliveryMethodName(String deliveryMethodName) {
        this.deliveryMethodName = deliveryMethodName;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Override
    public String toString() {
        return "DeliveryMethod{" +
               "deliveryMethodId=" + deliveryMethodId +
               ", deliveryMethodName='" + deliveryMethodName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
