package net.sf.persism.dao.wwi1;

import java.time.LocalDate;

public record Country(
        Integer countryId,
        String countryName,
        String formalName,
        String isoAlpha3Code,
        Integer isoNumericCode,
        String countryType,
        Long latestRecordedPopulation,
        String continent,
        String region,
        String subregion,
        Object border,
        Integer lastEditedBy,
        LocalDate validFrom,
        LocalDate validTo) {
}
