package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "TransactionTypes", schema = "Application", catalog = "WideWorldImporters")
public class TransactionType {
    private Integer transactionTypeId;
    private String transactionTypeName;
    private Integer lastEditedBy;
    private Date validFrom;
    private Date validTo;

    @Id
    @Column(name = "TransactionTypeID")
    public Integer getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(Integer transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    @Basic
    @Column(name = "TransactionTypeName")
    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
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
        TransactionType that = (TransactionType) o;
        return Objects.equals(transactionTypeId, that.transactionTypeId) && Objects.equals(transactionTypeName, that.transactionTypeName) && Objects.equals(lastEditedBy, that.lastEditedBy) && Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionTypeId, transactionTypeName, lastEditedBy, validFrom, validTo);
    }
}
