package net.sf.persism.dao.wwi1;

import net.sf.persism.PersistableObject;
import net.sf.persism.annotations.Column;

import java.sql.Date;
import java.util.Objects;

//@Table("Application.Cities")
//   extends PersistableObject because columns location, validFrom, validTo can not be set
public final class City extends PersistableObject<City> {

    private Integer cityId;
    private String cityName;
    private Integer stateProvinceId;
    private Object location;
    private Long latestRecordedPopulation;
    private Integer lastEditedBy;
    @Column(readOnly = true)
    private Date validFrom; // these are used by temporal tables - should not be referenced
    @Column(readOnly = true)
    private Date validTo;

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getStateProvinceId() {
        return stateProvinceId;
    }

    public void setStateProvinceId(Integer stateProvinceId) {
        this.stateProvinceId = stateProvinceId;
    }

    public Object getLocation() {
        return location;
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
        City city = (City) o;
        return Objects.equals(cityId, city.cityId) && Objects.equals(cityName, city.cityName) && Objects.equals(stateProvinceId, city.stateProvinceId) && Objects.equals(location, city.location) && Objects.equals(latestRecordedPopulation, city.latestRecordedPopulation) && Objects.equals(lastEditedBy, city.lastEditedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, cityName, stateProvinceId, location, latestRecordedPopulation, lastEditedBy);
    }

    @Override
    public String toString() {
        return "City{" +
                "cityId=" + cityId +
                ", cityName='" + cityName + '\'' +
                ", stateProvinceId=" + stateProvinceId +
                ", location=" + location +
                ", latestRecordedPopulation=" + latestRecordedPopulation +
                ", lastEditedBy=" + lastEditedBy +
                '}';
    }
}
