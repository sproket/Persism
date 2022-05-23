package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "PaymentMethods", schema = "Application", catalog = "WideWorldImporters")
public class PaymentMethod {
    private Integer paymentMethodId;
    private String paymentMethodName;
    private Integer lastEditedBy;
    private Date validFrom;
    private Date validTo;

    @Id
    @Column(name = "PaymentMethodID")
    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    @Basic
    @Column(name = "PaymentMethodName")
    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
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
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(paymentMethodId, that.paymentMethodId) && Objects.equals(paymentMethodName, that.paymentMethodName) && Objects.equals(lastEditedBy, that.lastEditedBy) && Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentMethodId, paymentMethodName, lastEditedBy, validFrom, validTo);
    }
}
