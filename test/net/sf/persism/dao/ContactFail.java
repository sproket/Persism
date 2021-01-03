package net.sf.persism.dao;

import net.sf.persism.annotations.TableName;

import java.sql.Date;
import java.util.Objects;
import java.util.UUID;
// This version has an extra field to fail a unit test and exercise the exception
@TableName("Contacts")
public final class ContactFail {
    private UUID identity;
    private UUID partnerId;
    private String type;
    private String firstname;
    private String lastname;
    private String contactName;
    private String company;
    private String division;
    private String email;
    private String address1;
    private String address2;
    private String city;
    private String stateProvince;
    private String zipPostalCode;
    private String country;
    private Date dateAdded;
    private Date lastModified;


    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

    public UUID getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(UUID partnerId) {
        this.partnerId = partnerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getZipPostalCode() {
        return zipPostalCode;
    }

    public void setZipPostalCode(String zipPostalCode) {
        this.zipPostalCode = zipPostalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    private boolean fail;
    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactFail contact = (ContactFail) o;
        return Objects.equals(identity, contact.identity) && Objects.equals(partnerId, contact.partnerId) && Objects.equals(type, contact.type) && Objects.equals(firstname, contact.firstname) && Objects.equals(lastname, contact.lastname) && Objects.equals(contactName, contact.contactName) && Objects.equals(company, contact.company) && Objects.equals(division, contact.division) && Objects.equals(email, contact.email) && Objects.equals(address1, contact.address1) && Objects.equals(address2, contact.address2) && Objects.equals(city, contact.city) && Objects.equals(stateProvince, contact.stateProvince) && Objects.equals(zipPostalCode, contact.zipPostalCode) && Objects.equals(country, contact.country) && Objects.equals(dateAdded, contact.dateAdded) && Objects.equals(lastModified, contact.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, partnerId, type, firstname, lastname, contactName, company, division, email, address1, address2, city, stateProvince, zipPostalCode, country, dateAdded, lastModified);
    }

    @Override
    public String toString() {
        return "Contact{" +
                "Id=" + identity +
                ", partnerId=" + partnerId +
                ", type='" + type + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", contactName='" + contactName + '\'' +
                ", company='" + company + '\'' +
                ", division='" + division + '\'' +
                ", email='" + email + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", city='" + city + '\'' +
                ", stateProvince='" + stateProvince + '\'' +
                ", zipPostalCode='" + zipPostalCode + '\'' +
                ", country='" + country + '\'' +
                ", dateAdded=" + dateAdded +
                ", lastModified=" + lastModified +
                '}';
    }
}
