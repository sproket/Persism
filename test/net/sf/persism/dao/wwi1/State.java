package net.sf.persism.dao.wwi1;

import net.sf.persism.annotations.Table;

@Table("StateProvinces")
public final class State {
    private Integer stateProvinceId;
    private String stateProvinceCode;
    private String stateProvinceName;
    private Integer countryId;
    private String salesTerritory;
    private Object border;
    private Long latestRecordedPopulation;
    private Integer lastEditedBy;


    public Integer getStateProvinceId() {
        return stateProvinceId;
    }

    public void setStateProvinceId(Integer stateProvinceId) {
        this.stateProvinceId = stateProvinceId;
    }

    public String getStateProvinceCode() {
        return stateProvinceCode;
    }

    public void setStateProvinceCode(String stateProvinceCode) {
        this.stateProvinceCode = stateProvinceCode;
    }

    public String getStateProvinceName() {
        return stateProvinceName;
    }

    public void setStateProvinceName(String stateProvinceName) {
        this.stateProvinceName = stateProvinceName;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public String getSalesTerritory() {
        return salesTerritory;
    }

    public void setSalesTerritory(String salesTerritory) {
        this.salesTerritory = salesTerritory;
    }

    public Object getBorder() {
        return border;
    }

    public void setBorder(Object border) {
        this.border = border;
    }

    public Long getLatestRecordedPopulation() {
        return latestRecordedPopulation;
    }

    public void setLatestRecordedPopulation(Long latestRecordedPopulation) {
        this.latestRecordedPopulation = latestRecordedPopulation;
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

        State state = (State) o;

        if (stateProvinceId != null ? !stateProvinceId.equals(state.stateProvinceId) : state.stateProvinceId != null) {
            return false;
        }
        if (stateProvinceCode != null ? !stateProvinceCode.equals(state.stateProvinceCode) : state.stateProvinceCode != null) {
            return false;
        }
        if (stateProvinceName != null ? !stateProvinceName.equals(state.stateProvinceName) : state.stateProvinceName != null) {
            return false;
        }
        if (countryId != null ? !countryId.equals(state.countryId) : state.countryId != null) {
            return false;
        }
        if (salesTerritory != null ? !salesTerritory.equals(state.salesTerritory) : state.salesTerritory != null) {
            return false;
        }
        if (border != null ? !border.equals(state.border) : state.border != null) {
            return false;
        }
        if (latestRecordedPopulation != null ? !latestRecordedPopulation.equals(state.latestRecordedPopulation) : state.latestRecordedPopulation != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(state.lastEditedBy) : state.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = stateProvinceId != null ? stateProvinceId.hashCode() : 0;
        result = 31 * result + (stateProvinceCode != null ? stateProvinceCode.hashCode() : 0);
        result = 31 * result + (stateProvinceName != null ? stateProvinceName.hashCode() : 0);
        result = 31 * result + (countryId != null ? countryId.hashCode() : 0);
        result = 31 * result + (salesTerritory != null ? salesTerritory.hashCode() : 0);
        result = 31 * result + (border != null ? border.hashCode() : 0);
        result = 31 * result + (latestRecordedPopulation != null ? latestRecordedPopulation.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
               "stateProvinceId=" + stateProvinceId +
               ", stateProvinceCode='" + stateProvinceCode + '\'' +
               ", stateProvinceName='" + stateProvinceName + '\'' +
               ", countryId=" + countryId +
               ", salesTerritory='" + salesTerritory + '\'' +
               ", border=" + border +
               ", latestRecordedPopulation=" + latestRecordedPopulation +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
