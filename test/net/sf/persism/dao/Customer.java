package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotColumn;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * General customer class for database types.
 *
 * @author Dan Howard
 * @since 5/23/12 10:40 AM
 */
public final class Customer {
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

    // properties are case Insensitive
    @Join(to = Invoice.class, onProperties = " CustomerId , sTatuS ", toProperties = "cusTomerId , status ")
    private Set<Invoice> invoices = new HashSet<>();

    @Join(to = Invoice.class, onProperties = " CustomerId , sTatuS ", toProperties = "cusTomerId , status ")
    private Invoice whatever;

    @NotColumn
    private Contact contact;

    private LocalDate testLocalDate;
    private LocalDateTime testLocalDateTime;

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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
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

    public Timestamp getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Timestamp dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public LocalDateTime getDateOfLastOrder() {
        return dateOfLastOrder;
    }

    public void setDateOfLastOrder(LocalDateTime dateOfLastOrder) {
        this.dateOfLastOrder = dateOfLastOrder;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    public LocalDate getTestLocalDate() {
        return testLocalDate;
    }

    public void setTestLocalDate(LocalDate testLocalDate) {
        this.testLocalDate = testLocalDate;
    }

    public LocalDateTime getTestLocalDateTime() {
        return testLocalDateTime;
    }

    public void setTestLocalDateTime(LocalDateTime testLocalDateTime) {
        this.testLocalDateTime = testLocalDateTime;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    public Invoice getWhatever() {
        return whatever;
    }

    public void setWhatever(Invoice whatever) {
        this.whatever = whatever;
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
               ", status=" + status +
               ", group=" + groupId +
               '}';
    }
}
