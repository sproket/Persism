package net.sf.persism.dao.wwi1.views;

import net.sf.persism.annotations.View;


@View("Customers")
public record CustomerViewFail(
        Integer customerId,
        String customerName,
        String customerCategoryName,
        String primaryContact,
        String alternateContact,
        String phoneNumber,
        String faxNumber,
        String buyingGroupName,
        String websiteUrl,
        String deliveryMethod,
        String cityName,
        Object deliveryLocation,
        String deliveryRun,
        String runPosition) {
}
