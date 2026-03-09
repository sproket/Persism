package net.sf.persism.dao.wwi1.views;

import net.sf.persism.annotations.View;

import java.math.BigDecimal;
import java.sql.Date;

@View
public record VehicleTemperature(
        Long vehicleTemperatureId,
        String vehicleRegistration,
        Integer chillerSensorNumber,
        Date recordedWhen,
        BigDecimal temperature,
        String fullSensorData) {
}
