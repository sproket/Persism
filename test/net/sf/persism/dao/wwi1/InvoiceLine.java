package net.sf.persism.dao.wwi1;

import java.math.BigDecimal;
import java.sql.Date;

// s
public final class InvoiceLine {
    private Integer invoiceLineId;
    private Integer invoiceId;
    private Integer stockItemId;
    private String description;
    private Integer packageTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal lineProfit;
    private BigDecimal extendedPrice;
    private Integer lastEditedBy;
    private Date lastEditedWhen;

    public Integer invoiceLineId() {
        return invoiceLineId;
    }

    public InvoiceLine setInvoiceLineId(Integer invoiceLineId) {
        this.invoiceLineId = invoiceLineId;
        return this;
    }

    public Integer invoiceId() {
        return invoiceId;
    }

    public InvoiceLine setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public Integer stockItemId() {
        return stockItemId;
    }

    public InvoiceLine setStockItemId(Integer stockItemId) {
        this.stockItemId = stockItemId;
        return this;
    }

    public String description() {
        return description;
    }

    public InvoiceLine setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer packageTypeId() {
        return packageTypeId;
    }

    public InvoiceLine setPackageTypeId(Integer packageTypeId) {
        this.packageTypeId = packageTypeId;
        return this;
    }

    public Integer quantity() {
        return quantity;
    }

    public InvoiceLine setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public InvoiceLine setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public BigDecimal taxRate() {
        return taxRate;
    }

    public InvoiceLine setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
        return this;
    }

    public BigDecimal taxAmount() {
        return taxAmount;
    }

    public InvoiceLine setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        return this;
    }

    public BigDecimal lineProfit() {
        return lineProfit;
    }

    public InvoiceLine setLineProfit(BigDecimal lineProfit) {
        this.lineProfit = lineProfit;
        return this;
    }

    public BigDecimal extendedPrice() {
        return extendedPrice;
    }

    public InvoiceLine setExtendedPrice(BigDecimal extendedPrice) {
        this.extendedPrice = extendedPrice;
        return this;
    }

    public Integer lastEditedBy() {
        return lastEditedBy;
    }

    public InvoiceLine setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
        return this;
    }

    public Date lastEditedWhen() {
        return lastEditedWhen;
    }

    public InvoiceLine setLastEditedWhen(Date lastEditedWhen) {
        this.lastEditedWhen = lastEditedWhen;
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

        InvoiceLine that = (InvoiceLine) o;

        if (invoiceLineId != null ? !invoiceLineId.equals(that.invoiceLineId) : that.invoiceLineId != null) {
            return false;
        }
        if (invoiceId != null ? !invoiceId.equals(that.invoiceId) : that.invoiceId != null) {
            return false;
        }
        if (stockItemId != null ? !stockItemId.equals(that.stockItemId) : that.stockItemId != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (packageTypeId != null ? !packageTypeId.equals(that.packageTypeId) : that.packageTypeId != null) {
            return false;
        }
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) {
            return false;
        }
        if (unitPrice != null ? !unitPrice.equals(that.unitPrice) : that.unitPrice != null) {
            return false;
        }
        if (taxRate != null ? !taxRate.equals(that.taxRate) : that.taxRate != null) {
            return false;
        }
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null) {
            return false;
        }
        if (lineProfit != null ? !lineProfit.equals(that.lineProfit) : that.lineProfit != null) {
            return false;
        }
        if (extendedPrice != null ? !extendedPrice.equals(that.extendedPrice) : that.extendedPrice != null) {
            return false;
        }
        if (lastEditedBy != null ? !lastEditedBy.equals(that.lastEditedBy) : that.lastEditedBy != null) {
            return false;
        }
        return lastEditedWhen != null ? lastEditedWhen.equals(that.lastEditedWhen) : that.lastEditedWhen == null;
    }

    @Override
    public int hashCode() {
        int result = invoiceLineId != null ? invoiceLineId.hashCode() : 0;
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (stockItemId != null ? stockItemId.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (packageTypeId != null ? packageTypeId.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        result = 31 * result + (taxRate != null ? taxRate.hashCode() : 0);
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (lineProfit != null ? lineProfit.hashCode() : 0);
        result = 31 * result + (extendedPrice != null ? extendedPrice.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        result = 31 * result + (lastEditedWhen != null ? lastEditedWhen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InvoiceLine{" +
               "invoiceLineId=" + invoiceLineId +
               ", invoiceId=" + invoiceId +
               ", stockItemId=" + stockItemId +
               ", description='" + description + '\'' +
               ", packageTypeId=" + packageTypeId +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", taxRate=" + taxRate +
               ", taxAmount=" + taxAmount +
               ", lineProfit=" + lineProfit +
               ", extendedPrice=" + extendedPrice +
               ", lastEditedBy=" + lastEditedBy +
               ", lastEditedWhen=" + lastEditedWhen +
               '}';
    }
}
