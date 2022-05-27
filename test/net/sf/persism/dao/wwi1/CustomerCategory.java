package net.sf.persism.dao.wwi1;

// salse
public final class CustomerCategory {
    private Integer customerCategoryId;
    private String customerCategoryName;
    private Integer lastEditedBy;

    public Integer customerCategoryId() {
        return customerCategoryId;
    }

    public CustomerCategory setCustomerCategoryId(Integer customerCategoryId) {
        this.customerCategoryId = customerCategoryId;
        return this;
    }

    public String customerCategoryName() {
        return customerCategoryName;
    }

    public CustomerCategory setCustomerCategoryName(String customerCategoryName) {
        this.customerCategoryName = customerCategoryName;
        return this;
    }

    public Integer lastEditedBy() {
        return lastEditedBy;
    }

    public CustomerCategory setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerCategory that = (CustomerCategory) o;

        if (customerCategoryId != null ? !customerCategoryId.equals(that.customerCategoryId) : that.customerCategoryId != null) {
            return false;
        }
        if (customerCategoryName != null ? !customerCategoryName.equals(that.customerCategoryName) : that.customerCategoryName != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = customerCategoryId != null ? customerCategoryId.hashCode() : 0;
        result = 31 * result + (customerCategoryName != null ? customerCategoryName.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomerCategory{" +
               "customerCategoryId=" + customerCategoryId +
               ", customerCategoryName='" + customerCategoryName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
