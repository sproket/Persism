package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;

// ware
public final class StockItemStockGroup {
    private Integer stockItemStockGroupId;
    private Integer stockItemId;
    private Integer stockGroupId;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getStockItemStockGroupId() {
        return stockItemStockGroupId;
    }

    public void setStockItemStockGroupId(Integer stockItemStockGroupId) {
        this.stockItemStockGroupId = stockItemStockGroupId;
    }

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public Integer getStockGroupId() {
        return stockGroupId;
    }

    public void setStockGroupId(Integer stockGroupId) {
        this.stockGroupId = stockGroupId;
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

        StockItemStockGroup that = (StockItemStockGroup) o;

        if (stockItemStockGroupId != null ? !stockItemStockGroupId.equals(that.stockItemStockGroupId) : that.stockItemStockGroupId != null) {
            return false;
        }
        if (stockItemId != null ? !stockItemId.equals(that.stockItemId) : that.stockItemId != null) {
            return false;
        }
        if (stockGroupId != null ? !stockGroupId.equals(that.stockGroupId) : that.stockGroupId != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = stockItemStockGroupId != null ? stockItemStockGroupId.hashCode() : 0;
        result = 31 * result + (stockItemId != null ? stockItemId.hashCode() : 0);
        result = 31 * result + (stockGroupId != null ? stockGroupId.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockItemStockGroup{" +
               "stockItemStockGroupId=" + stockItemStockGroupId +
               ", stockItemId=" + stockItemId +
               ", stockGroupId=" + stockGroupId +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
