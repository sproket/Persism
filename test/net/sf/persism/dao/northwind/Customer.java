package net.sf.persism.dao.northwind;

import net.sf.persism.annotations.NotColumn;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;

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
    private java.util.Date dateOfLastResort;
    private Date dateOfDoom;
    // TODO Does not work with JTDS and can't be parsed right now - Do with other zone related issues
    @NotColumn
    private LocalDateTime dateOfOffset; // this is a DateTimeOffset in the DB - KEEP to test for normal DateTime or double check because we use this with DB retuning the type as VARCHAR
    private LocalDateTime testLocalDateTime;

    private Instant nowMF;

    private String wtfDate;

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

    public java.util.Date getDateOfLastResort() {
        return dateOfLastResort;
    }

    public void setDateOfLastResort(java.util.Date dateOfLastResort) {
        this.dateOfLastResort = dateOfLastResort;
    }

    public Date getDateOfDoom() {
        return dateOfDoom;
    }

    public void setDateOfDoom(Date dateOfDoom) {
        this.dateOfDoom = dateOfDoom;
    }

    public LocalDateTime getDateOfOffset() {
        return dateOfOffset;
    }

    public void setDateOfOffset(LocalDateTime dateOfOffset) {
        this.dateOfOffset = dateOfOffset;
    }

    public Instant getNowMF() {
        return nowMF;
    }

    public void setNowMF(Instant nowMF) {
        this.nowMF = nowMF;
    }

    public String getWtfDate() {
        return wtfDate;
    }

    public void setWtfDate(String wtfDate) {
        this.wtfDate = wtfDate;
    }

    public LocalDateTime getTestLocalDateTime() {
        return testLocalDateTime;
    }

    public void setTestLocalDateTime(LocalDateTime testLocalDateTime) {
        this.testLocalDateTime = testLocalDateTime;
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
