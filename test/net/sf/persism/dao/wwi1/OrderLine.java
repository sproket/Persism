package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;

// s
public final class OrderLine {
    private Integer orderLineId;
    private Integer orderId;
    private Integer stockItemId;
    private String description;
    private Integer packageTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private Integer pickedQuantity;
    private Date pickingCompletedWhen;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(Integer orderLineId) {
        this.orderLineId = orderLineId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(Integer packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Integer getPickedQuantity() {
        return pickedQuantity;
    }

    public void setPickedQuantity(Integer pickedQuantity) {
        this.pickedQuantity = pickedQuantity;
    }

    public Date getPickingCompletedWhen() {
        return pickingCompletedWhen;
    }

    public void setPickingCompletedWhen(Date pickingCompletedWhen) {
        this.pickingCompletedWhen = pickingCompletedWhen;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public Date getLastEditedWhen() {
        return lastEditedWhen;
    }

    public void setLastEditedWhen(Date lastEditedWhen) {
        this.lastEditedWhen = lastEditedWhen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrderLine orderLine = (OrderLine) o;

        if (orderLineId != null ? !orderLineId.equals(orderLine.orderLineId) : orderLine.orderLineId != null) {
            return false;
        }
        if (orderId != null ? !orderId.equals(orderLine.orderId) : orderLine.orderId != null) {
            return false;
        }
        if (stockItemId != null ? !stockItemId.equals(orderLine.stockItemId) : orderLine.stockItemId != null) {
            return false;
        }
        if (description != null ? !description.equals(orderLine.description) : orderLine.description != null) {
            return false;
        }
        if (packageTypeId != null ? !packageTypeId.equals(orderLine.packageTypeId) : orderLine.packageTypeId != null) {
            return false;
        }
        if (quantity != null ? !quantity.equals(orderLine.quantity) : orderLine.quantity != null) {
            return false;
        }
        if (unitPrice != null ? !unitPrice.equals(orderLine.unitPrice) : orderLine.unitPrice != null) {
            return false;
        }
        if (taxRate != null ? !taxRate.equals(orderLine.taxRate) : orderLine.taxRate != null) {
            return false;
        }
        if (pickedQuantity != null ? !pickedQuantity.equals(orderLine.pickedQuantity) : orderLine.pickedQuantity != null) {
            return false;
        }
        if (pickingCompletedWhen != null ? !pickingCompletedWhen.equals(orderLine.pickingCompletedWhen) : orderLine.pickingCompletedWhen != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(orderLine.lastEditedBy) : orderLine.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(orderLine.lastEditedWhen) : orderLine.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = orderLineId != null ? orderLineId.hashCode() : 0;
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        result = 31 * result + (stockItemId != null ? stockItemId.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (packageTypeId != null ? packageTypeId.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        result = 31 * result + (taxRate != null ? taxRate.hashCode() : 0);
        result = 31 * result + (pickedQuantity != null ? pickedQuantity.hashCode() : 0);
        result = 31 * result + (pickingCompletedWhen != null ? pickingCompletedWhen.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrderLine{" +
               "orderLineId=" + orderLineId +
               ", orderId=" + orderId +
               ", stockItemId=" + stockItemId +
               ", description='" + description + '\'' +
               ", packageTypeId=" + packageTypeId +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", taxRate=" + taxRate +
               ", pickedQuantity=" + pickedQuantity +
               ", pickingCompletedWhen=" + pickingCompletedWhen +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
