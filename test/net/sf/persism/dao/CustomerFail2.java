package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Table("CustomerTABLEDOESNTEXIST")
public class CustomerFail2 {

    private String customerId;
    private int groupId;
    private String companyName;
    private String contactName;
    private String contactTitle;
    private String address;
    private String city;
    private Region region;
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

    public CustomerFail2 setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }


    public int groupId() {
        return groupId;
    }

    public CustomerFail2 setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    public String companyName() {
        return companyName;
    }

    public CustomerFail2 setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String contactName() {
        return contactName;
    }

    public CustomerFail2 setContactName(String contactName) {
        this.contactName = contactName;
        return this;
    }

    public String contactTitle() {
        return contactTitle;
    }

    public CustomerFail2 setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
        return this;
    }

    public String address() {
        return address;
    }

    public CustomerFail2 setAddress(String address) {
        this.address = address;
        return this;
    }

    public String city() {
        return city;
    }

    public CustomerFail2 setCity(String city) {
        this.city = city;
        return this;
    }

    public Region region() {
        return region;
    }

    public CustomerFail2 setRegion(Region region) {
        this.region = region;
        return this;
    }

    public String postalCode() {
        return postalCode;
    }

    public CustomerFail2 setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public String country() {
        return country;
    }

    public CustomerFail2 setCountry(String country) {
        this.country = country;
        return this;
    }

    public String phone() {
        return phone;
    }

    public CustomerFail2 setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String fax() {
        return fax;
    }

    public CustomerFail2 setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public Character status() {
        return status;
    }

    public CustomerFail2 setStatus(Character status) {
        this.status = status;
        return this;
    }

    public Timestamp dateRegistered() {
        return dateRegistered;
    }

    public CustomerFail2 setDateRegistered(Timestamp dateRegistered) {
        this.dateRegistered = dateRegistered;
        return this;
    }

    public LocalDateTime dateOfLastOrder() {
        return dateOfLastOrder;
    }

    public CustomerFail2 setDateOfLastOrder(LocalDateTime dateOfLastOrder) {
        this.dateOfLastOrder = dateOfLastOrder;
        return this;
    }
}
