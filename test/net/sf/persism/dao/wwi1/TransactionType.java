package net.sf.persism.dao.wwi1;

import java.sql.Date;

public final class TransactionType {
    private Integer transactionTypeId;
    private String transactionTypeName;
    private Integer lastEditedBy;

    public Integer getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(Integer transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Override
    public String toString() {
        return "TransactionType{" +
               "transactionTypeId=" + transactionTypeId +
               ", transactionTypeName='" + transactionTypeName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
