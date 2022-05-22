package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Table("Customers")
public class CustomerFail {

    @Column(primary = true, hasDefault = true)
    private String customerId;

    private int groupId;
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
    private Character status;

    @Column(hasDefault = true)
    private Timestamp dateRegistered;

    private LocalDateTime dateOfLastOrder;

    public String customerId() {
        return customerId;
    }

    public CustomerFail setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }


    public int groupId() {
        return groupId;
    }

    public CustomerFail setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    public String companyName() {
        return companyName;
    }

    public CustomerFail setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String contactName() {
        return contactName;
    }

    public CustomerFail setContactName(String contactName) {
        this.contactName = contactName;
        return this;
    }

    public String contactTitle() {
        return contactTitle;
    }

    public CustomerFail setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
        return this;
    }

    public String address() {
        return address;
    }

    public CustomerFail setAddress(String address) {
        this.address = address;
        return this;
    }

    public String city() {
        return city;
    }

    public CustomerFail setCity(String city) {
        this.city = city;
        return this;
    }

    public Regions region() {
        return region;
    }

    public CustomerFail setRegion(Regions region) {
        this.region = region;
        return this;
    }

    public String postalCode() {
        return postalCode;
    }

    public CustomerFail setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public String country() {
        return country;
    }

    public CustomerFail setCountry(String country) {
        this.country = country;
        return this;
    }

    public String phone() {
        return phone;
    }

    public CustomerFail setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String fax() {
        return fax;
    }

    public CustomerFail setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public Character status() {
        return status;
    }

    public CustomerFail setStatus(Character status) {
        this.status = status;
        return this;
    }

    public Timestamp dateRegistered() {
        return dateRegistered;
    }

    public CustomerFail setDateRegistered(Timestamp dateRegistered) {
        this.dateRegistered = dateRegistered;
        return this;
    }

    public LocalDateTime dateOfLastOrder() {
        return dateOfLastOrder;
    }

    public CustomerFail setDateOfLastOrder(LocalDateTime dateOfLastOrder) {
        this.dateOfLastOrder = dateOfLastOrder;
        return this;
    }
}
