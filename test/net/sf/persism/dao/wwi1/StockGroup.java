package net.sf.persism.dao.wwi1;

// war
public final class StockGroup {
    private Integer stockGroupId;
    private String stockGroupName;
    private Integer lastEditedBy;

    public Integer getStockGroupId() {
        return stockGroupId;
    }

    public void setStockGroupId(Integer stockGroupId) {
        this.stockGroupId = stockGroupId;
    }

    public String getStockGroupName() {
        return stockGroupName;
    }

    public void setStockGroupName(String stockGroupName) {
        this.stockGroupName = stockGroupName;
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

        StockGroup that = (StockGroup) o;

        if (stockGroupId != null ? !stockGroupId.equals(that.stockGroupId) : that.stockGroupId != null) {
            return false;
        }
        if (stockGroupName != null ? !stockGroupName.equals(that.stockGroupName) : that.stockGroupName != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = stockGroupId != null ? stockGroupId.hashCode() : 0;
        result = 31 * result + (stockGroupName != null ? stockGroupName.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockGroup{" +
               "stockGroupId=" + stockGroupId +
               ", stockGroupName='" + stockGroupName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
