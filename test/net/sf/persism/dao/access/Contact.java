package net.sf.persism.dao.access;

import net.ucanaccess.complex.Attachment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Contact  { // implements Persistable<Contact>

    private int id;
    private String company;
    private String lastName;
    private String firstName;

//    @Column(name = "E-mail Address")
    private String emailAddress;

//    @Column(name = "ZIP/Postal Code")
    private String ZIPPostalCode;

    private String jobTitle;

    private String businessPhone;
    private String homePhone;
    private String mobilePhone;
    private String faxNumber;
    private String address;
    private String city;

//    @Column(name = "State/Province")
    private String stateProvince;

    //@Column(name = "Country/Region")
    private String countryRegion;

    private String webPage;

    private String notes;

//    private Object Attachments;  // this would work for unknown type - up to u to cast it or whatever.
    private Attachment[] attachments;  // This works if you know the specific type - you can see it logged as a warning

    private LocalDateTime created;

    private String category;

    private BigDecimal howMuchTheyOweMe;

    public BigDecimal getHowMuchTheyOweMe() {
        return howMuchTheyOweMe;
    }

    public void setHowMuchTheyOweMe(BigDecimal howMuchTheyOweMe) {
        this.howMuchTheyOweMe = howMuchTheyOweMe;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getZIPPostalCode() {
        return ZIPPostalCode;
    }

    public void setZIPPostalCode(String ZIPPostalCode) {
        this.ZIPPostalCode = ZIPPostalCode;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
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

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Attachment[] getAttachments() {
        return attachments;
    }

    public void setAttachments(Attachment[] attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "\nContact{" +
                "id=" + id +
                ", company='" + company + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", ZIPPostalCode='" + ZIPPostalCode + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", businessPhone='" + businessPhone + '\'' +
                ", homePhone='" + homePhone + '\'' +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", faxNumber='" + faxNumber + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", stateProvince='" + stateProvince + '\'' +
                ", countryRegion='" + countryRegion + '\'' +
                ", webPage='" + webPage + '\'' +
                ", notes='" + notes + '\'' +
                ", created=" + created +
                ", category='" + category + '\'' +
                ", howMuchTheyOweMe=" + howMuchTheyOweMe +
                "}";
    }

    //    private Contact originalValue;
//
//    @Override
//    public void saveReadState() throws PersismException {
//        originalValue = clone();
//    }
//
//    @Override
//    public Contact getOriginalValue() {
//        return originalValue;
//    }
//
//    @Override
//    public Contact clone() {
//        try {
//            return (Contact) super.clone();
//        } catch (CloneNotSupportedException e) {
//            throw new PersismException(e.getMessage(), e);
//        }
//    }

}