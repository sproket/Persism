package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "StateProvinces", schema = "Application", catalog = "WideWorldImporters")
public class State {
    private Integer stateProvinceId;
    private String stateProvinceCode;
    private String stateProvinceName;
    private Integer countryId;
    private String salesTerritory;
    private Object border;
    private Long latestRecordedPopulation;
    private Integer lastEditedBy;
    private Date validFrom;
    private Date validTo;

    @Id
    @Column(name = "StateProvinceID")
    public Integer getStateProvinceId() {
        return stateProvinceId;
    }

    public void setStateProvinceId(Integer stateProvinceId) {
        this.stateProvinceId = stateProvinceId;
    }

    @Basic
    @Column(name = "StateProvinceCode")
    public String getStateProvinceCode() {
        return stateProvinceCode;
    }

    public void setStateProvinceCode(String stateProvinceCode) {
        this.stateProvinceCode = stateProvinceCode;
    }

    @Basic
    @Column(name = "StateProvinceName")
    public String getStateProvinceName() {
        return stateProvinceName;
    }

    public void setStateProvinceName(String stateProvinceName) {
        this.stateProvinceName = stateProvinceName;
    }

    @Basic
    @Column(name = "CountryID")
    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    @Basic
    @Column(name = "SalesTerritory")
    public String getSalesTerritory() {
        return salesTerritory;
    }

    public void setSalesTerritory(String salesTerritory) {
        this.salesTerritory = salesTerritory;
    }

    @Basic
    @Column(name = "Border")
    public Object getBorder() {
        return border;
    }

    public void setBorder(Object border) {
        this.border = border;
    }

    @Basic
    @Column(name = "LatestRecordedPopulation")
    public Long getLatestRecordedPopulation() {
        return latestRecordedPopulation;
    }

    public void setLatestRecordedPopulation(Long latestRecordedPopulation) {
        this.latestRecordedPopulation = latestRecordedPopulation;
    }

    @Basic
    @Column(name = "LastEditedBy")
    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Basic
    @Column(name = "ValidFrom")
    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    @Basic
    @Column(name = "ValidTo")
    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
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
        return Objects.equals(stateProvinceId, state.stateProvinceId) && Objects.equals(stateProvinceCode, state.stateProvinceCode) && Objects.equals(stateProvinceName, state.stateProvinceName) && Objects.equals(countryId, state.countryId) && Objects.equals(salesTerritory, state.salesTerritory) && Objects.equals(border, state.border) && Objects.equals(latestRecordedPopulation, state.latestRecordedPopulation) && Objects.equals(lastEditedBy, state.lastEditedBy) && Objects.equals(validFrom, state.validFrom) && Objects.equals(validTo, state.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateProvinceId, stateProvinceCode, stateProvinceName, countryId, salesTerritory, border, latestRecordedPopulation, lastEditedBy, validFrom, validTo);
    }
}
