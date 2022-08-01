package net.sf.persism.dao.wwi1;

import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.Date;
public final class Customer {
    private Integer customerId;
    private String customerName;
    private Integer billToCustomerId;
    private Integer customerCategoryId;
    private Integer buyingGroupId;
    private Integer primaryContactPersonId;
    private Integer alternateContactPersonId;
    private Integer deliveryMethodId;
    private Integer deliveryCityId;
    private Integer postalCityId;
    private BigDecimal creditLimit;
    private Date accountOpenedDate;
    private BigDecimal standardDiscountPercentage;
    private Boolean isStatementSent;
    private Boolean isOnCreditHold;
    private Integer paymentDays;
    private String phoneNumber;
    private String faxNumber;
    private String deliveryRun;
    private String runPosition;
    private String websiteUrl;
    private String deliveryAddressLine1;
    private String deliveryAddressLine2;
    private String deliveryPostalCode;
    private Object deliveryLocation;
    private String postalAddressLine1;
    private String postalAddressLine2;
    private String postalPostalCode;
    private Integer lastEditedBy;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getBillToCustomerId() {
        return billToCustomerId;
    }

    public void setBillToCustomerId(Integer billToCustomerId) {
        this.billToCustomerId = billToCustomerId;
    }

    public Integer getCustomerCategoryId() {
        return customerCategoryId;
    }

    public void setCustomerCategoryId(Integer customerCategoryId) {
        this.customerCategoryId = customerCategoryId;
    }

    public Integer getBuyingGroupId() {
        return buyingGroupId;
    }

    public void setBuyingGroupId(Integer buyingGroupId) {
        this.buyingGroupId = buyingGroupId;
    }

    public Integer getPrimaryContactPersonId() {
        return primaryContactPersonId;
    }

    public void setPrimaryContactPersonId(Integer primaryContactPersonId) {
        this.primaryContactPersonId = primaryContactPersonId;
    }

    public Integer getAlternateContactPersonId() {
        return alternateContactPersonId;
    }

    public void setAlternateContactPersonId(Integer alternateContactPersonId) {
        this.alternateContactPersonId = alternateContactPersonId;
    }

    public Integer getDeliveryMethodId() {
        return deliveryMethodId;
    }

    public void setDeliveryMethodId(Integer deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
    }

    public Integer getDeliveryCityId() {
        return deliveryCityId;
    }

    public void setDeliveryCityId(Integer deliveryCityId) {
        this.deliveryCityId = deliveryCityId;
    }

    public Integer getPostalCityId() {
        return postalCityId;
    }

    public void setPostalCityId(Integer postalCityId) {
        this.postalCityId = postalCityId;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Date getAccountOpenedDate() {
        return accountOpenedDate;
    }

    public void setAccountOpenedDate(Date accountOpenedDate) {
        this.accountOpenedDate = accountOpenedDate;
    }

    public BigDecimal getStandardDiscountPercentage() {
        return standardDiscountPercentage;
    }

    public void setStandardDiscountPercentage(BigDecimal standardDiscountPercentage) {
        this.standardDiscountPercentage = standardDiscountPercentage;
    }

    public Boolean getStatementSent() {
        return isStatementSent;
    }

    public void setStatementSent(Boolean statementSent) {
        isStatementSent = statementSent;
    }

    public Boolean getOnCreditHold() {
        return isOnCreditHold;
    }

    public void setOnCreditHold(Boolean onCreditHold) {
        isOnCreditHold = onCreditHold;
    }

    public Integer getPaymentDays() {
        return paymentDays;
    }

    public void setPaymentDays(Integer paymentDays) {
        this.paymentDays = paymentDays;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getDeliveryRun() {
        return deliveryRun;
    }

    public void setDeliveryRun(String deliveryRun) {
        this.deliveryRun = deliveryRun;
    }

    public String getRunPosition() {
        return runPosition;
    }

    public void setRunPosition(String runPosition) {
        this.runPosition = runPosition;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getDeliveryAddressLine1() {
        return deliveryAddressLine1;
    }

    public void setDeliveryAddressLine1(String deliveryAddressLine1) {
        this.deliveryAddressLine1 = deliveryAddressLine1;
    }

    public String getDeliveryAddressLine2() {
        return deliveryAddressLine2;
    }

    public void setDeliveryAddressLine2(String deliveryAddressLine2) {
        this.deliveryAddressLine2 = deliveryAddressLine2;
    }

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    public void setDeliveryPostalCode(String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }

    public Object getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(Object deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getPostalAddressLine1() {
        return postalAddressLine1;
    }

    public void setPostalAddressLine1(String postalAddressLine1) {
        this.postalAddressLine1 = postalAddressLine1;
    }

    public String getPostalAddressLine2() {
        return postalAddressLine2;
    }

    public void setPostalAddressLine2(String postalAddressLine2) {
        this.postalAddressLine2 = postalAddressLine2;
    }

    public String getPostalPostalCode() {
        return postalPostalCode;
    }

    public void setPostalPostalCode(String postalPostalCode) {
        this.postalPostalCode = postalPostalCode;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Override
    public String toString() {
        return "Customer{" +
               "customerId=" + customerId +
               ", customerName='" + customerName + '\'' +
               ", billToCustomerId=" + billToCustomerId +
               ", customerCategoryId=" + customerCategoryId +
               ", buyingGroupId=" + buyingGroupId +
               ", primaryContactPersonId=" + primaryContactPersonId +
               ", alternateContactPersonId=" + alternateContactPersonId +
               ", deliveryMethodId=" + deliveryMethodId +
               ", deliveryCityId=" + deliveryCityId +
               ", postalCityId=" + postalCityId +
               ", creditLimit=" + creditLimit +
               ", accountOpenedDate=" + accountOpenedDate +
               ", standardDiscountPercentage=" + standardDiscountPercentage +
               ", isStatementSent=" + isStatementSent +
               ", isOnCreditHold=" + isOnCreditHold +
               ", paymentDays=" + paymentDays +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", faxNumber='" + faxNumber + '\'' +
               ", deliveryRun='" + deliveryRun + '\'' +
               ", runPosition='" + runPosition + '\'' +
               ", websiteUrl='" + websiteUrl + '\'' +
               ", deliveryAddressLine1='" + deliveryAddressLine1 + '\'' +
               ", deliveryAddressLine2='" + deliveryAddressLine2 + '\'' +
               ", deliveryPostalCode='" + deliveryPostalCode + '\'' +
               ", deliveryLocation=" + deliveryLocation +
               ", postalAddressLine1='" + postalAddressLine1 + '\'' +
               ", postalAddressLine2='" + postalAddressLine2 + '\'' +
               ", postalPostalCode='" + postalPostalCode + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
