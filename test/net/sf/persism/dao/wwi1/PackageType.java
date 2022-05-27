package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;

// ware
public final class PackageType {
    private Integer packageTypeId;
    private String packageTypeName;
    private Integer lastEditedBy;

    public Integer getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(Integer packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public String getPackageTypeName() {
        return packageTypeName;
    }

    public void setPackageTypeName(String packageTypeName) {
        this.packageTypeName = packageTypeName;
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

        PackageType that = (PackageType) o;

        if (packageTypeId != null ? !packageTypeId.equals(that.packageTypeId) : that.packageTypeId != null) {
            return false;
        }
        if (packageTypeName != null ? !packageTypeName.equals(that.packageTypeName) : that.packageTypeName != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = packageTypeId != null ? packageTypeId.hashCode() : 0;
        result = 31 * result + (packageTypeName != null ? packageTypeName.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PackageType{" +
               "packageTypeId=" + packageTypeId +
               ", packageTypeName='" + packageTypeName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
