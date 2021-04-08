package net.sf.persism.dao.northwind;

import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * test for Record style where getters don't start with "get" and instead match the field name.
 *
 * @author Dan Howard
 * @since 5/25/12 5:59 AM
 */
@NotTable
public record OrderView(
        int orderId,
        String customerId,
        int employeeId,
        Date orderDate,
        Date requiredDate,
        Date shippedDate,
        int productId,
        float unitPrice,
        int quantity,
        BigDecimal discount,
        String customerName,
        String employeeName,
        String productName
        ) {
}
