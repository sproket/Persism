package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

// purchasiung
public final class PurchaseOrderLine {
    private Integer purchaseOrderLineId;
    private Integer purchaseOrderId;
    private Integer stockItemId;
    private Integer orderedOuters;
    private String description;
    private Integer receivedOuters;
    private Integer packageTypeId;
    private BigDecimal expectedUnitPricePerOuter;
    private Date lastReceiptDate;
    private Boolean isOrderLineFinalized;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getPurchaseOrderLineId() {
        return purchaseOrderLineId;
    }

    public void setPurchaseOrderLineId(Integer purchaseOrderLineId) {
        this.purchaseOrderLineId = purchaseOrderLineId;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public Integer getOrderedOuters() {
        return orderedOuters;
    }

    public void setOrderedOuters(Integer orderedOuters) {
        this.orderedOuters = orderedOuters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getReceivedOuters() {
        return receivedOuters;
    }

    public void setReceivedOuters(Integer receivedOuters) {
        this.receivedOuters = receivedOuters;
    }

    public Integer getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(Integer packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public BigDecimal getExpectedUnitPricePerOuter() {
        return expectedUnitPricePerOuter;
    }

    public void setExpectedUnitPricePerOuter(BigDecimal expectedUnitPricePerOuter) {
        this.expectedUnitPricePerOuter = expectedUnitPricePerOuter;
    }

    public Date getLastReceiptDate() {
        return lastReceiptDate;
    }

    public void setLastReceiptDate(Date lastReceiptDate) {
        this.lastReceiptDate = lastReceiptDate;
    }

    public Boolean getOrderLineFinalized() {
        return isOrderLineFinalized;
    }

    public void setOrderLineFinalized(Boolean orderLineFinalized) {
        isOrderLineFinalized = orderLineFinalized;
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

        PurchaseOrderLine that = (PurchaseOrderLine) o;

        if (purchaseOrderLineId != null ? !purchaseOrderLineId.equals(that.purchaseOrderLineId) : that.purchaseOrderLineId != null) {
            return false;
        }
        if (purchaseOrderId != null ? !purchaseOrderId.equals(that.purchaseOrderId) : that.purchaseOrderId != null) {
            return false;
        }
        if (stockItemId != null ? !stockItemId.equals(that.stockItemId) : that.stockItemId != null) {
            return false;
        }
        if (orderedOuters != null ? !orderedOuters.equals(that.orderedOuters) : that.orderedOuters != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (receivedOuters != null ? !receivedOuters.equals(that.receivedOuters) : that.receivedOuters != null) {
            return false;
        }
        if (packageTypeId != null ? !packageTypeId.equals(that.packageTypeId) : that.packageTypeId != null) {
            return false;
        }
        if (expectedUnitPricePerOuter != null ? !expectedUnitPricePerOuter.equals(that.expectedUnitPricePerOuter) : that.expectedUnitPricePerOuter != null) {
            return false;
        }
        if (lastReceiptDate != null ? !lastReceiptDate.equals(that.lastReceiptDate) : that.lastReceiptDate != null) {
            return false;
        }
        if (isOrderLineFinalized != null ? !isOrderLineFinalized.equals(that.isOrderLineFinalized) : that.isOrderLineFinalized != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = purchaseOrderLineId != null ? purchaseOrderLineId.hashCode() : 0;
        result = 31 * result + (purchaseOrderId != null ? purchaseOrderId.hashCode() : 0);
        result = 31 * result + (stockItemId != null ? stockItemId.hashCode() : 0);
        result = 31 * result + (orderedOuters != null ? orderedOuters.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (receivedOuters != null ? receivedOuters.hashCode() : 0);
        result = 31 * result + (packageTypeId != null ? packageTypeId.hashCode() : 0);
        result = 31 * result + (expectedUnitPricePerOuter != null ? expectedUnitPricePerOuter.hashCode() : 0);
        result = 31 * result + (lastReceiptDate != null ? lastReceiptDate.hashCode() : 0);
        result = 31 * result + (isOrderLineFinalized != null ? isOrderLineFinalized.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PurchaseOrderLine{" +
               "purchaseOrderLineId=" + purchaseOrderLineId +
               ", purchaseOrderId=" + purchaseOrderId +
               ", stockItemId=" + stockItemId +
               ", orderedOuters=" + orderedOuters +
               ", description='" + description + '\'' +
               ", receivedOuters=" + receivedOuters +
               ", packageTypeId=" + packageTypeId +
               ", expectedUnitPricePerOuter=" + expectedUnitPricePerOuter +
               ", lastReceiptDate=" + lastReceiptDate +
               ", isOrderLineFinalized=" + isOrderLineFinalized +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
