package net.sf.persism.dao.wwi1;

public final class SupplierCategory {
    private Integer supplierCategoryId;
    private String supplierCategoryName;
    private Integer lastEditedBy;

    public Integer getSupplierCategoryId() {
        return supplierCategoryId;
    }

    public void setSupplierCategoryId(Integer supplierCategoryId) {
        this.supplierCategoryId = supplierCategoryId;
    }

    public String getSupplierCategoryName() {
        return supplierCategoryName;
    }

    public void setSupplierCategoryName(String supplierCategoryName) {
        this.supplierCategoryName = supplierCategoryName;
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

        SupplierCategory that = (SupplierCategory) o;

        if (supplierCategoryId != null ? !supplierCategoryId.equals(that.supplierCategoryId) : that.supplierCategoryId != null) {
            return false;
        }
        if (supplierCategoryName != null ? !supplierCategoryName.equals(that.supplierCategoryName) : that.supplierCategoryName != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = supplierCategoryId != null ? supplierCategoryId.hashCode() : 0;
        result = 31 * result + (supplierCategoryName != null ? supplierCategoryName.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SupplierCategory{" +
               "supplierCategoryId=" + supplierCategoryId +
               ", supplierCategoryName='" + supplierCategoryName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
