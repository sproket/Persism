package net.sf.persism.dao.wwi1;

import java.sql.Date;

// Sales
public class BuyingGroup {
    private Integer buyingGroupId;
    private String buyingGroupName;
    private Integer lastEditedBy;

    private Date validFrom;
    private Date validTo;

    public Integer buyingGroupId() {
        return buyingGroupId;
    }

    public BuyingGroup setBuyingGroupId(Integer buyingGroupId) {
        this.buyingGroupId = buyingGroupId;
        return this;
    }

    public String buyingGroupName() {
        return buyingGroupName;
    }

    public BuyingGroup setBuyingGroupName(String buyingGroupName) {
        this.buyingGroupName = buyingGroupName;
        return this;
    }

    public Integer lastEditedBy() {
        return lastEditedBy;
    }

    public BuyingGroup setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
        return this;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuyingGroup that = (BuyingGroup) o;

        if (buyingGroupId != null ? !buyingGroupId.equals(that.buyingGroupId) : that.buyingGroupId != null) {
            return false;
        }
        if (buyingGroupName != null ? !buyingGroupName.equals(that.buyingGroupName) : that.buyingGroupName != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = buyingGroupId != null ? buyingGroupId.hashCode() : 0;
        result = 31 * result + (buyingGroupName != null ? buyingGroupName.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BuyingGroup{" +
               "buyingGroupId=" + buyingGroupId +
               ", buyingGroupName='" + buyingGroupName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
