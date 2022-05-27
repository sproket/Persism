package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;

// Warehouse
public final class ColdRoomTemperature {

    private Long coldRoomTemperatureId;
    private Integer coldRoomSensorNumber;
    private Date recordedWhen;
    private BigDecimal temperature;

    public Long coldRoomTemperatureId() {
        return coldRoomTemperatureId;
    }

    public ColdRoomTemperature setColdRoomTemperatureId(Long coldRoomTemperatureId) {
        this.coldRoomTemperatureId = coldRoomTemperatureId;
        return this;
    }

    public Integer coldRoomSensorNumber() {
        return coldRoomSensorNumber;
    }

    public ColdRoomTemperature setColdRoomSensorNumber(Integer coldRoomSensorNumber) {
        this.coldRoomSensorNumber = coldRoomSensorNumber;
        return this;
    }

    public Date recordedWhen() {
        return recordedWhen;
    }

    public ColdRoomTemperature setRecordedWhen(Date recordedWhen) {
        this.recordedWhen = recordedWhen;
        return this;
    }

    public BigDecimal temperature() {
        return temperature;
    }

    public ColdRoomTemperature setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
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

        ColdRoomTemperature that = (ColdRoomTemperature) o;

        if (coldRoomTemperatureId != null ? !coldRoomTemperatureId.equals(that.coldRoomTemperatureId) : that.coldRoomTemperatureId != null) {
            return false;
        }
        if (coldRoomSensorNumber != null ? !coldRoomSensorNumber.equals(that.coldRoomSensorNumber) : that.coldRoomSensorNumber != null) {
            return false;
        }
        if (recordedWhen != null ? !recordedWhen.equals(that.recordedWhen) : that.recordedWhen != null) {
            return false;
        }
        return temperature != null ? temperature.equals(that.temperature) : that.temperature == null;
    }

    @Override
    public int hashCode() {
        int result = coldRoomTemperatureId != null ? coldRoomTemperatureId.hashCode() : 0;
        result = 31 * result + (coldRoomSensorNumber != null ? coldRoomSensorNumber.hashCode() : 0);
        result = 31 * result + (recordedWhen != null ? recordedWhen.hashCode() : 0);
        result = 31 * result + (temperature != null ? temperature.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ColdRoomTemperature{" +
               "coldRoomTemperatureId=" + coldRoomTemperatureId +
               ", coldRoomSensorNumber=" + coldRoomSensorNumber +
               ", recordedWhen=" + recordedWhen +
               ", temperature=" + temperature +
               '}';
    }
}
