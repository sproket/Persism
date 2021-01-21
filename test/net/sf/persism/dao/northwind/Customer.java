package net.sf.persism.dao.northwind;

import java.sql.Date;

/**
 * NORTHWIND CUSTOMER OBJECT
 *
 * @author Dan Howard
 * @since 5/4/12 5:52 AM
 */
public final class Customer {

    private String customerId;
    private String companyName;
    private String contactName;
    private String contactTitle;
    private String address;
    private String city;
    private String region; // note Northwind can't use the Regions Enum since that db has different junk in that field.
    private String postalCode;
    private String country;
    private String phone;
    private String fax;
    private Date dateOfLastResort;
    private Date dateOfDoom;
    private Date dateOfOffset;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
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

    public Date getDateOfLastResort() {
        return dateOfLastResort;
    }

    public void setDateOfLastResort(Date dateOfLastResort) {
        this.dateOfLastResort = dateOfLastResort;
    }

    public Date getDateOfDoom() {
        return dateOfDoom;
    }

    public void setDateOfDoom(Date dateOfDoom) {
        this.dateOfDoom = dateOfDoom;
    }

    public Date getDateOfOffset() {
        return dateOfOffset;
    }

    public void setDateOfOffset(Date dateOfOffset) {
        this.dateOfOffset = dateOfOffset;
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
                ", dateOfLastResort='" + dateOfLastResort + '\'' +
                ", dateOfDoom='" + dateOfDoom + '\'' +
                ", dateOfOffset='" + dateOfOffset + '\'' +
                '}';
    }
}
