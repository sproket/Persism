package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.Table;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("Customers")
public class CustomerFail4 {

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

    @Join(to = Invoice.class, onProperties = "CustomerId,Status", toProperties = "CustomerId, Status")
    private final List<Invoice> invoices = new ArrayList<>();

    // misspell getter or missing getter - same effect.
    public List<Invoice> getInviices() {
        return invoices;
    }

    public String customerId() {
        return customerId;
    }

    public CustomerFail4 setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }


    public int groupId() {
        return groupId;
    }

    public CustomerFail4 setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    public String companyName() {
        return companyName;
    }

    public CustomerFail4 setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String contactName() {
        return contactName;
    }

    public CustomerFail4 setContactName(String contactName) {
        this.contactName = contactName;
        return this;
    }

    public String contactTitle() {
        return contactTitle;
    }

    public CustomerFail4 setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
        return this;
    }

    public String address() {
        return address;
    }

    public CustomerFail4 setAddress(String address) {
        this.address = address;
        return this;
    }

    public String city() {
        return city;
    }

    public CustomerFail4 setCity(String city) {
        this.city = city;
        return this;
    }

    public Region region() {
        return region;
    }

    public CustomerFail4 setRegion(Region region) {
        this.region = region;
        return this;
    }

    public String postalCode() {
        return postalCode;
    }

    public CustomerFail4 setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public String country() {
        return country;
    }

    public CustomerFail4 setCountry(String country) {
        this.country = country;
        return this;
    }

    public String phone() {
        return phone;
    }

    public CustomerFail4 setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String fax() {
        return fax;
    }

    public CustomerFail4 setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public Character status() {
        return status;
    }

    public CustomerFail4 setStatus(Character status) {
        this.status = status;
        return this;
    }

    public Timestamp dateRegistered() {
        return dateRegistered;
    }

    public CustomerFail4 setDateRegistered(Timestamp dateRegistered) {
        this.dateRegistered = dateRegistered;
        return this;
    }

    public LocalDateTime dateOfLastOrder() {
        return dateOfLastOrder;
    }

    public CustomerFail4 setDateOfLastOrder(LocalDateTime dateOfLastOrder) {
        this.dateOfLastOrder = dateOfLastOrder;
        return this;
    }
}
