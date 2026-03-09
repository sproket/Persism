package net.sf.persism.dao.wwi1.views;

import net.sf.persism.annotations.View;

@View
public record Supplier(
        Integer supplierId,
        String supplierName,
        String supplierCategoryName,
        String primaryContact,
        String alternateContact,
        String phoneNumber,
        String faxNumber,
        String websiteUrl,
        String deliveryMethod,
        String cityName,
        Object deliveryLocation,
        String supplierReference) {
}
