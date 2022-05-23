package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "DeliveryMethods", schema = "Application", catalog = "WideWorldImporters")
public class DeliveryMethod {
    private Integer deliveryMethodId;
    private String deliveryMethodName;
    private Integer lastEditedBy;
    private Date validFrom;
    private Date validTo;

    @Id
    @Column(name = "DeliveryMethodID")
    public Integer getDeliveryMethodId() {
        return deliveryMethodId;
    }

    public void setDeliveryMethodId(Integer deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
    }

    @Basic
    @Column(name = "DeliveryMethodName")
    public String getDeliveryMethodName() {
        return deliveryMethodName;
    }

    public void setDeliveryMethodName(String deliveryMethodName) {
        this.deliveryMethodName = deliveryMethodName;
    }

    @Basic
    @Column(name = "LastEditedBy")
    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Basic
    @Column(name = "ValidFrom")
    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    @Basic
    @Column(name = "ValidTo")
    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeliveryMethod that = (DeliveryMethod) o;
        return Objects.equals(deliveryMethodId, that.deliveryMethodId) && Objects.equals(deliveryMethodName, that.deliveryMethodName) && Objects.equals(lastEditedBy, that.lastEditedBy) && Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryMethodId, deliveryMethodName, lastEditedBy, validFrom, validTo);
    }
}
