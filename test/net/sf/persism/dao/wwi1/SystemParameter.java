package net.sf.persism.dao.wwi1;

import java.sql.Date;

public final class SystemParameter {
    private Integer systemParameterId;
    private String deliveryAddressLine1;
    private String deliveryAddressLine2;
    private Integer deliveryCityId;
    private String deliveryPostalCode;
    private Object deliveryLocation;
    private String postalAddressLine1;
    private String postalAddressLine2;
    private Integer postalCityId;
    private String postalPostalCode;
    private String applicationSettings;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getSystemParameterId() {
        return systemParameterId;
    }

    public void setSystemParameterId(Integer systemParameterId) {
        this.systemParameterId = systemParameterId;
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

    public Integer getDeliveryCityId() {
        return deliveryCityId;
    }

    public void setDeliveryCityId(Integer deliveryCityId) {
        this.deliveryCityId = deliveryCityId;
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

    public Integer getPostalCityId() {
        return postalCityId;
    }

    public void setPostalCityId(Integer postalCityId) {
        this.postalCityId = postalCityId;
    }

    public String getPostalPostalCode() {
        return postalPostalCode;
    }

    public void setPostalPostalCode(String postalPostalCode) {
        this.postalPostalCode = postalPostalCode;
    }

    public String getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(String applicationSettings) {
        this.applicationSettings = applicationSettings;
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

        SystemParameter that = (SystemParameter) o;

        if (systemParameterId != null ? !systemParameterId.equals(that.systemParameterId) : that.systemParameterId != null) {
            return false;
        }
        if (deliveryAddressLine1 != null ? !deliveryAddressLine1.equals(that.deliveryAddressLine1) : that.deliveryAddressLine1 != null) {
            return false;
        }
        if (deliveryAddressLine2 != null ? !deliveryAddressLine2.equals(that.deliveryAddressLine2) : that.deliveryAddressLine2 != null) {
            return false;
        }
        if (deliveryCityId != null ? !deliveryCityId.equals(that.deliveryCityId) : that.deliveryCityId != null) {
            return false;
        }
        if (deliveryPostalCode != null ? !deliveryPostalCode.equals(that.deliveryPostalCode) : that.deliveryPostalCode != null) {
            return false;
        }
        if (deliveryLocation != null ? !deliveryLocation.equals(that.deliveryLocation) : that.deliveryLocation != null) {
            return false;
        }
        if (postalAddressLine1 != null ? !postalAddressLine1.equals(that.postalAddressLine1) : that.postalAddressLine1 != null) {
            return false;
        }
        if (postalAddressLine2 != null ? !postalAddressLine2.equals(that.postalAddressLine2) : that.postalAddressLine2 != null) {
            return false;
        }
        if (postalCityId != null ? !postalCityId.equals(that.postalCityId) : that.postalCityId != null) {
            return false;
        }
        if (postalPostalCode != null ? !postalPostalCode.equals(that.postalPostalCode) : that.postalPostalCode != null) {
            return false;
        }
        if (applicationSettings != null ? !applicationSettings.equals(that.applicationSettings) : that.applicationSettings != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = systemParameterId != null ? systemParameterId.hashCode() : 0;
        result = 31 * result + (deliveryAddressLine1 != null ? deliveryAddressLine1.hashCode() : 0);
        result = 31 * result + (deliveryAddressLine2 != null ? deliveryAddressLine2.hashCode() : 0);
        result = 31 * result + (deliveryCityId != null ? deliveryCityId.hashCode() : 0);
        result = 31 * result + (deliveryPostalCode != null ? deliveryPostalCode.hashCode() : 0);
        result = 31 * result + (deliveryLocation != null ? deliveryLocation.hashCode() : 0);
        result = 31 * result + (postalAddressLine1 != null ? postalAddressLine1.hashCode() : 0);
        result = 31 * result + (postalAddressLine2 != null ? postalAddressLine2.hashCode() : 0);
        result = 31 * result + (postalCityId != null ? postalCityId.hashCode() : 0);
        result = 31 * result + (postalPostalCode != null ? postalPostalCode.hashCode() : 0);
        result = 31 * result + (applicationSettings != null ? applicationSettings.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SystemParameter{" +
               "systemParameterId=" + systemParameterId +
               ", deliveryAddressLine1='" + deliveryAddressLine1 + '\'' +
               ", deliveryAddressLine2='" + deliveryAddressLine2 + '\'' +
               ", deliveryCityId=" + deliveryCityId +
               ", deliveryPostalCode='" + deliveryPostalCode + '\'' +
               ", deliveryLocation=" + deliveryLocation +
               ", postalAddressLine1='" + postalAddressLine1 + '\'' +
               ", postalAddressLine2='" + postalAddressLine2 + '\'' +
               ", postalCityId=" + postalCityId +
               ", postalPostalCode='" + postalPostalCode + '\'' +
               ", applicationSettings='" + applicationSettings + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
