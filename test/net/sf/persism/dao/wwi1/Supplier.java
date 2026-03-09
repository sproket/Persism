package net.sf.persism.dao.wwi1;

public final class Supplier {
    private Integer supplierId;
    private String supplierName;
    private Integer supplierCategoryId;
    private Integer primaryContactPersonId;
    private Integer alternateContactPersonId;
    private Integer deliveryMethodId;
    private Integer deliveryCityId;
    private Integer postalCityId;
    private String supplierReference;
    private String bankAccountName;
    private String bankAccountBranch;
    private String bankAccountCode;
    private String bankAccountNumber;
    private String bankInternationalCode;
    private Integer paymentDays;
    private String internalComments;
    private String phoneNumber;
    private String faxNumber;
    private String websiteUrl;
    private String deliveryAddressLine1;
    private String deliveryAddressLine2;
    private String deliveryPostalCode;
    private Object deliveryLocation;
    private String postalAddressLine1;
    private String postalAddressLine2;
    private String postalPostalCode;
    private Integer lastEditedBy;

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getSupplierCategoryId() {
        return supplierCategoryId;
    }

    public void setSupplierCategoryId(Integer supplierCategoryId) {
        this.supplierCategoryId = supplierCategoryId;
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

    public String getSupplierReference() {
        return supplierReference;
    }

    public void setSupplierReference(String supplierReference) {
        this.supplierReference = supplierReference;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getBankAccountBranch() {
        return bankAccountBranch;
    }

    public void setBankAccountBranch(String bankAccountBranch) {
        this.bankAccountBranch = bankAccountBranch;
    }

    public String getBankAccountCode() {
        return bankAccountCode;
    }

    public void setBankAccountCode(String bankAccountCode) {
        this.bankAccountCode = bankAccountCode;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankInternationalCode() {
        return bankInternationalCode;
    }

    public void setBankInternationalCode(String bankInternationalCode) {
        this.bankInternationalCode = bankInternationalCode;
    }

    public Integer getPaymentDays() {
        return paymentDays;
    }

    public void setPaymentDays(Integer paymentDays) {
        this.paymentDays = paymentDays;
    }

    public String getInternalComments() {
        return internalComments;
    }

    public void setInternalComments(String internalComments) {
        this.internalComments = internalComments;
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
        return "Supplier{" +
               "supplierId=" + supplierId +
               ", supplierName='" + supplierName + '\'' +
               ", supplierCategoryId=" + supplierCategoryId +
               ", primaryContactPersonId=" + primaryContactPersonId +
               ", alternateContactPersonId=" + alternateContactPersonId +
               ", deliveryMethodId=" + deliveryMethodId +
               ", deliveryCityId=" + deliveryCityId +
               ", postalCityId=" + postalCityId +
               ", supplierReference='" + supplierReference + '\'' +
               ", bankAccountName='" + bankAccountName + '\'' +
               ", bankAccountBranch='" + bankAccountBranch + '\'' +
               ", bankAccountCode='" + bankAccountCode + '\'' +
               ", bankAccountNumber='" + bankAccountNumber + '\'' +
               ", bankInternationalCode='" + bankInternationalCode + '\'' +
               ", paymentDays=" + paymentDays +
               ", internalComments='" + internalComments + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", faxNumber='" + faxNumber + '\'' +
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
