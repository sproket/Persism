package net.sf.persism.dao.wwi1;

public final class PaymentMethod {
    private Integer paymentMethodId;
    private String paymentMethodName;
    private Integer lastEditedBy;

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
               "paymentMethodId=" + paymentMethodId +
               ", paymentMethodName='" + paymentMethodName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
