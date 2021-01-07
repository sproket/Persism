package net.sf.persism.dao;

import net.sf.persism.PersismException;
import net.sf.persism.Persistable;
import net.sf.persism.annotations.Column;

import java.io.Serializable;
import java.sql.Date;

/**
 * General customer class for database types.
 * todo long is not supported at the Bean level for dates
 *
 * @author Dan Howard
 * @since 5/23/12 10:40 AM
 */
public class Customer {

    private String customerId;
    private String companyName;
    private String contactName;
    private String contactTitle;
    private String address;
    private String city;
    private Regions region;
    private String postalCode;
    private String country;
    private String phone;
    private String fax;

    @Column (hasDefault = true)
    private java.util.Date dateRegistered;

    private java.sql.Date dateOfLastOrder;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactTitle() {
        return contactTitle;
    }

    public void setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Regions getRegion() {
        return region;
    }

    public void setRegion(Regions region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public java.util.Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(java.util.Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public java.sql.Date getDateOfLastOrder() {
        return dateOfLastOrder;
    }

    public void setDateOfLastOrder(java.sql.Date dateOfLastOrder) {
        this.dateOfLastOrder = dateOfLastOrder;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactTitle='" + contactTitle + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", region=" + region +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", phone='" + phone + '\'' +
                ", fax='" + fax + '\'' +
                ", dateRegistered=" + dateRegistered +
                ", dateOfLastOrder=" + dateOfLastOrder +
                '}';
    }
}
