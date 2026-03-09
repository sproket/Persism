package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;

public final class VehicleTemperature {

    private Long vehicleTemperatureId;
    private String vehicleRegistration;
    private Integer chillerSensorNumber;
    private Date recordedWhen;
    private BigDecimal temperature;
    private String fullSensorData;
    private Boolean isCompressed;
    private byte[] compressedSensorData;

    public Long getVehicleTemperatureId() {
        return vehicleTemperatureId;
    }

    public void setVehicleTemperatureId(Long vehicleTemperatureId) {
        this.vehicleTemperatureId = vehicleTemperatureId;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public Integer getChillerSensorNumber() {
        return chillerSensorNumber;
    }

    public void setChillerSensorNumber(Integer chillerSensorNumber) {
        this.chillerSensorNumber = chillerSensorNumber;
    }

    public Date getRecordedWhen() {
        return recordedWhen;
    }

    public void setRecordedWhen(Date recordedWhen) {
        this.recordedWhen = recordedWhen;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public String getFullSensorData() {
        return fullSensorData;
    }

    public void setFullSensorData(String fullSensorData) {
        this.fullSensorData = fullSensorData;
    }

    public Boolean getCompressed() {
        return isCompressed;
    }

    public void setCompressed(Boolean compressed) {
        isCompressed = compressed;
    }

    public byte[] getCompressedSensorData() {
        return compressedSensorData;
    }

    public void setCompressedSensorData(byte[] compressedSensorData) {
        this.compressedSensorData = compressedSensorData;
    }

    @Override
    public String toString() {
        return "VehicleTemperature{" +
               "vehicleTemperatureId=" + vehicleTemperatureId +
               ", vehicleRegistration='" + vehicleRegistration + '\'' +
               ", chillerSensorNumber=" + chillerSensorNumber +
               ", recordedWhen=" + recordedWhen +
               ", temperature=" + temperature +
               ", fullSensorData='" + fullSensorData + '\'' +
               ", isCompressed=" + isCompressed +
               ", compressedSensorData=" + Arrays.toString(compressedSensorData) +
               '}';
    }
}
