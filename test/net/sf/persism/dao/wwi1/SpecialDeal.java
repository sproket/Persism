package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;

// s ?@Entity
public final class SpecialDeal {
    private Integer specialDealId;
    private Integer stockItemId;
    private Integer customerId;
    private Integer buyingGroupId;
    private Integer customerCategoryId;
    private Integer stockGroupId;
    private String dealDescription;
    private Date startDate;
    private Date endDate;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal unitPrice;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getSpecialDealId() {
        return specialDealId;
    }

    public void setSpecialDealId(Integer specialDealId) {
        this.specialDealId = specialDealId;
    }

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getBuyingGroupId() {
        return buyingGroupId;
    }

    public void setBuyingGroupId(Integer buyingGroupId) {
        this.buyingGroupId = buyingGroupId;
    }

    public Integer getCustomerCategoryId() {
        return customerCategoryId;
    }

    public void setCustomerCategoryId(Integer customerCategoryId) {
        this.customerCategoryId = customerCategoryId;
    }

    public Integer getStockGroupId() {
        return stockGroupId;
    }

    public void setStockGroupId(Integer stockGroupId) {
        this.stockGroupId = stockGroupId;
    }

    public String getDealDescription() {
        return dealDescription;
    }

    public void setDealDescription(String dealDescription) {
        this.dealDescription = dealDescription;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

        SpecialDeal that = (SpecialDeal) o;

        if (specialDealId != null ? !specialDealId.equals(that.specialDealId) : that.specialDealId != null) {
            return false;
        }
        if (stockItemId != null ? !stockItemId.equals(that.stockItemId) : that.stockItemId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) {
            return false;
        }
        if (buyingGroupId != null ? !buyingGroupId.equals(that.buyingGroupId) : that.buyingGroupId != null) {
            return false;
        }
        if (customerCategoryId != null ? !customerCategoryId.equals(that.customerCategoryId) : that.customerCategoryId != null) {
            return false;
        }
        if (stockGroupId != null ? !stockGroupId.equals(that.stockGroupId) : that.stockGroupId != null) {
            return false;
        }
        if (dealDescription != null ? !dealDescription.equals(that.dealDescription) : that.dealDescription != null) {
            return false;
        }
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) {
            return false;
        }
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) {
            return false;
        }
        if (discountAmount != null ? !discountAmount.equals(that.discountAmount) : that.discountAmount != null) {
            return false;
        }
        if (discountPercentage != null ? !discountPercentage.equals(that.discountPercentage) : that.discountPercentage != null) {
            return false;
        }
        if (unitPrice != null ? !unitPrice.equals(that.unitPrice) : that.unitPrice != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = specialDealId != null ? specialDealId.hashCode() : 0;
        result = 31 * result + (stockItemId != null ? stockItemId.hashCode() : 0);
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (buyingGroupId != null ? buyingGroupId.hashCode() : 0);
        result = 31 * result + (customerCategoryId != null ? customerCategoryId.hashCode() : 0);
        result = 31 * result + (stockGroupId != null ? stockGroupId.hashCode() : 0);
        result = 31 * result + (dealDescription != null ? dealDescription.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (discountAmount != null ? discountAmount.hashCode() : 0);
        result = 31 * result + (discountPercentage != null ? discountPercentage.hashCode() : 0);
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SpecialDeal{" +
               "specialDealId=" + specialDealId +
               ", stockItemId=" + stockItemId +
               ", customerId=" + customerId +
               ", buyingGroupId=" + buyingGroupId +
               ", customerCategoryId=" + customerCategoryId +
               ", stockGroupId=" + stockGroupId +
               ", dealDescription='" + dealDescription + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", discountAmount=" + discountAmount +
               ", discountPercentage=" + discountPercentage +
               ", unitPrice=" + unitPrice +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
