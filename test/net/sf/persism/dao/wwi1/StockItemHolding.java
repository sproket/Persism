package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

public final class StockItemHolding {
    private Integer stockItemId;
    private Integer quantityOnHand;
    private String binLocation;
    private Integer lastStocktakeQuantity;
    private BigDecimal lastCostPrice;
    private Integer reorderLevel;
    private Integer targetStockLevel;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public String getBinLocation() {
        return binLocation;
    }

    public void setBinLocation(String binLocation) {
        this.binLocation = binLocation;
    }

    public Integer getLastStocktakeQuantity() {
        return lastStocktakeQuantity;
    }

    public void setLastStocktakeQuantity(Integer lastStocktakeQuantity) {
        this.lastStocktakeQuantity = lastStocktakeQuantity;
    }

    public BigDecimal getLastCostPrice() {
        return lastCostPrice;
    }

    public void setLastCostPrice(BigDecimal lastCostPrice) {
        this.lastCostPrice = lastCostPrice;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Integer getTargetStockLevel() {
        return targetStockLevel;
    }

    public void setTargetStockLevel(Integer targetStockLevel) {
        this.targetStockLevel = targetStockLevel;
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

        StockItemHolding that = (StockItemHolding) o;

        if (stockItemId != null ? !stockItemId.equals(that.stockItemId) : that.stockItemId != null) {
            return false;
        }
        if (quantityOnHand != null ? !quantityOnHand.equals(that.quantityOnHand) : that.quantityOnHand != null) {
            return false;
        }
        if (binLocation != null ? !binLocation.equals(that.binLocation) : that.binLocation != null) {
            return false;
        }
        if (lastStocktakeQuantity != null ? !lastStocktakeQuantity.equals(that.lastStocktakeQuantity) : that.lastStocktakeQuantity != null) {
            return false;
        }
        if (lastCostPrice != null ? !lastCostPrice.equals(that.lastCostPrice) : that.lastCostPrice != null) {
            return false;
        }
        if (reorderLevel != null ? !reorderLevel.equals(that.reorderLevel) : that.reorderLevel != null) {
            return false;
        }
        if (targetStockLevel != null ? !targetStockLevel.equals(that.targetStockLevel) : that.targetStockLevel != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = stockItemId != null ? stockItemId.hashCode() : 0;
        result = 31 * result + (quantityOnHand != null ? quantityOnHand.hashCode() : 0);
        result = 31 * result + (binLocation != null ? binLocation.hashCode() : 0);
        result = 31 * result + (lastStocktakeQuantity != null ? lastStocktakeQuantity.hashCode() : 0);
        result = 31 * result + (lastCostPrice != null ? lastCostPrice.hashCode() : 0);
        result = 31 * result + (reorderLevel != null ? reorderLevel.hashCode() : 0);
        result = 31 * result + (targetStockLevel != null ? targetStockLevel.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockItemHolding{" +
               "stockItemId=" + stockItemId +
               ", quantityOnHand=" + quantityOnHand +
               ", binLocation='" + binLocation + '\'' +
               ", lastStocktakeQuantity=" + lastStocktakeQuantity +
               ", lastCostPrice=" + lastCostPrice +
               ", reorderLevel=" + reorderLevel +
               ", targetStockLevel=" + targetStockLevel +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
