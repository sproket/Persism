package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;

// wa
public final class StockItem {
    private Integer stockItemId;
    private String stockItemName;
    private Integer supplierId;
    private Integer colorId;
    private Integer unitPackageId;
    private Integer outerPackageId;
    private String brand;
    private String size;
    private Integer leadTimeDays;
    private Integer quantityPerOuter;
    private Boolean isChillerStock;
    private String barcode;
    private BigDecimal taxRate;
    private BigDecimal unitPrice;
    private BigDecimal recommendedRetailPrice;
    private BigDecimal typicalWeightPerUnit;
    private String marketingComments;
    private String internalComments;
    private byte[] photo;
    private String customFields;
    private String tags;
    private String searchDetails;
    private Integer lastEditedBy;

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public String getStockItemName() {
        return stockItemName;
    }

    public void setStockItemName(String stockItemName) {
        this.stockItemName = stockItemName;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getColorId() {
        return colorId;
    }

    public void setColorId(Integer colorId) {
        this.colorId = colorId;
    }

    public Integer getUnitPackageId() {
        return unitPackageId;
    }

    public void setUnitPackageId(Integer unitPackageId) {
        this.unitPackageId = unitPackageId;
    }

    public Integer getOuterPackageId() {
        return outerPackageId;
    }

    public void setOuterPackageId(Integer outerPackageId) {
        this.outerPackageId = outerPackageId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public Integer getQuantityPerOuter() {
        return quantityPerOuter;
    }

    public void setQuantityPerOuter(Integer quantityPerOuter) {
        this.quantityPerOuter = quantityPerOuter;
    }

    public Boolean getChillerStock() {
        return isChillerStock;
    }

    public void setChillerStock(Boolean chillerStock) {
        isChillerStock = chillerStock;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(BigDecimal recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    public BigDecimal getTypicalWeightPerUnit() {
        return typicalWeightPerUnit;
    }

    public void setTypicalWeightPerUnit(BigDecimal typicalWeightPerUnit) {
        this.typicalWeightPerUnit = typicalWeightPerUnit;
    }

    public String getMarketingComments() {
        return marketingComments;
    }

    public void setMarketingComments(String marketingComments) {
        this.marketingComments = marketingComments;
    }

    public String getInternalComments() {
        return internalComments;
    }

    public void setInternalComments(String internalComments) {
        this.internalComments = internalComments;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getCustomFields() {
        return customFields;
    }

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSearchDetails() {
        return searchDetails;
    }

    public void setSearchDetails(String searchDetails) {
        this.searchDetails = searchDetails;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StockItem stockItem = (StockItem) o;

        if (stockItemId != null ? !stockItemId.equals(stockItem.stockItemId) : stockItem.stockItemId != null) {
            return false;
        }
        if (stockItemName != null ? !stockItemName.equals(stockItem.stockItemName) : stockItem.stockItemName != null) {
            return false;
        }
        if (supplierId != null ? !supplierId.equals(stockItem.supplierId) : stockItem.supplierId != null) {
            return false;
        }
        if (colorId != null ? !colorId.equals(stockItem.colorId) : stockItem.colorId != null) {
            return false;
        }
        if (unitPackageId != null ? !unitPackageId.equals(stockItem.unitPackageId) : stockItem.unitPackageId != null) {
            return false;
        }
        if (outerPackageId != null ? !outerPackageId.equals(stockItem.outerPackageId) : stockItem.outerPackageId != null) {
            return false;
        }
        if (brand != null ? !brand.equals(stockItem.brand) : stockItem.brand != null) {
            return false;
        }
        if (size != null ? !size.equals(stockItem.size) : stockItem.size != null) {
            return false;
        }
        if (leadTimeDays != null ? !leadTimeDays.equals(stockItem.leadTimeDays) : stockItem.leadTimeDays != null) {
            return false;
        }
        if (quantityPerOuter != null ? !quantityPerOuter.equals(stockItem.quantityPerOuter) : stockItem.quantityPerOuter != null) {
            return false;
        }
        if (isChillerStock != null ? !isChillerStock.equals(stockItem.isChillerStock) : stockItem.isChillerStock != null) {
            return false;
        }
        if (barcode != null ? !barcode.equals(stockItem.barcode) : stockItem.barcode != null) {
            return false;
        }
        if (taxRate != null ? !taxRate.equals(stockItem.taxRate) : stockItem.taxRate != null) {
            return false;
        }
        if (unitPrice != null ? !unitPrice.equals(stockItem.unitPrice) : stockItem.unitPrice != null) {
            return false;
        }
        if (recommendedRetailPrice != null ? !recommendedRetailPrice.equals(stockItem.recommendedRetailPrice) : stockItem.recommendedRetailPrice != null) {
            return false;
        }
        if (typicalWeightPerUnit != null ? !typicalWeightPerUnit.equals(stockItem.typicalWeightPerUnit) : stockItem.typicalWeightPerUnit != null) {
            return false;
        }
        if (marketingComments != null ? !marketingComments.equals(stockItem.marketingComments) : stockItem.marketingComments != null) {
            return false;
        }
        if (internalComments != null ? !internalComments.equals(stockItem.internalComments) : stockItem.internalComments != null) {
            return false;
        }
        if (!Arrays.equals(photo, stockItem.photo)) {
            return false;
        }
        if (customFields != null ? !customFields.equals(stockItem.customFields) : stockItem.customFields != null) {
            return false;
        }
        if (tags != null ? !tags.equals(stockItem.tags) : stockItem.tags != null) {
            return false;
        }
        if (searchDetails != null ? !searchDetails.equals(stockItem.searchDetails) : stockItem.searchDetails != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(stockItem.lastEditedBy) : stockItem.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = stockItemId != null ? stockItemId.hashCode() : 0;
        result = 31 * result + (stockItemName != null ? stockItemName.hashCode() : 0);
        result = 31 * result + (supplierId != null ? supplierId.hashCode() : 0);
        result = 31 * result + (colorId != null ? colorId.hashCode() : 0);
        result = 31 * result + (unitPackageId != null ? unitPackageId.hashCode() : 0);
        result = 31 * result + (outerPackageId != null ? outerPackageId.hashCode() : 0);
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (leadTimeDays != null ? leadTimeDays.hashCode() : 0);
        result = 31 * result + (quantityPerOuter != null ? quantityPerOuter.hashCode() : 0);
        result = 31 * result + (isChillerStock != null ? isChillerStock.hashCode() : 0);
        result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
        result = 31 * result + (taxRate != null ? taxRate.hashCode() : 0);
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        result = 31 * result + (recommendedRetailPrice != null ? recommendedRetailPrice.hashCode() : 0);
        result = 31 * result + (typicalWeightPerUnit != null ? typicalWeightPerUnit.hashCode() : 0);
        result = 31 * result + (marketingComments != null ? marketingComments.hashCode() : 0);
        result = 31 * result + (internalComments != null ? internalComments.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(photo);
        result = 31 * result + (customFields != null ? customFields.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (searchDetails != null ? searchDetails.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockItem{" +
               "stockItemId=" + stockItemId +
               ", stockItemName='" + stockItemName + '\'' +
               ", supplierId=" + supplierId +
               ", colorId=" + colorId +
               ", unitPackageId=" + unitPackageId +
               ", outerPackageId=" + outerPackageId +
               ", brand='" + brand + '\'' +
               ", size='" + size + '\'' +
               ", leadTimeDays=" + leadTimeDays +
               ", quantityPerOuter=" + quantityPerOuter +
               ", isChillerStock=" + isChillerStock +
               ", barcode='" + barcode + '\'' +
               ", taxRate=" + taxRate +
               ", unitPrice=" + unitPrice +
               ", recommendedRetailPrice=" + recommendedRetailPrice +
               ", typicalWeightPerUnit=" + typicalWeightPerUnit +
               ", marketingComments='" + marketingComments + '\'' +
               ", internalComments='" + internalComments + '\'' +
               ", photo=" + Arrays.toString(photo) +
               ", customFields='" + customFields + '\'' +
               ", tags='" + tags + '\'' +
               ", searchDetails='" + searchDetails + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
