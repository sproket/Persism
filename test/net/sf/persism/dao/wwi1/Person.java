package net.sf.persism.dao.wwi1;

import javax.persistence.*;
import java.sql.Date;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "People", schema = "Application", catalog = "WideWorldImporters")
public class Person {
    private Integer personId;
    private String fullName;
    private String preferredName;
    private String searchName;
    private Boolean isPermittedToLogon;
    private String logonName;
    private Boolean isExternalLogonProvider;
    private byte[] hashedPassword;
    private Boolean isSystemUser;
    private Boolean isEmployee;
    private Boolean isSalesperson;
    private String userPreferences;
    private String phoneNumber;
    private String faxNumber;
    private String emailAddress;
    private byte[] photo;
    private String customFields;
    private String otherLanguages;
    private Integer lastEditedBy;
    private Date validFrom;
    private Date validTo;

    @Id
    @Column(name = "PersonID")
    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    @Basic
    @Column(name = "FullName")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Basic
    @Column(name = "PreferredName")
    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    @Basic
    @Column(name = "SearchName")
    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    @Basic
    @Column(name = "IsPermittedToLogon")
    public Boolean getPermittedToLogon() {
        return isPermittedToLogon;
    }

    public void setPermittedToLogon(Boolean permittedToLogon) {
        isPermittedToLogon = permittedToLogon;
    }

    @Basic
    @Column(name = "LogonName")
    public String getLogonName() {
        return logonName;
    }

    public void setLogonName(String logonName) {
        this.logonName = logonName;
    }

    @Basic
    @Column(name = "IsExternalLogonProvider")
    public Boolean getExternalLogonProvider() {
        return isExternalLogonProvider;
    }

    public void setExternalLogonProvider(Boolean externalLogonProvider) {
        isExternalLogonProvider = externalLogonProvider;
    }

    @Basic
    @Column(name = "HashedPassword")
    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Basic
    @Column(name = "IsSystemUser")
    public Boolean getSystemUser() {
        return isSystemUser;
    }

    public void setSystemUser(Boolean systemUser) {
        isSystemUser = systemUser;
    }

    @Basic
    @Column(name = "IsEmployee")
    public Boolean getEmployee() {
        return isEmployee;
    }

    public void setEmployee(Boolean employee) {
        isEmployee = employee;
    }

    @Basic
    @Column(name = "IsSalesperson")
    public Boolean getSalesperson() {
        return isSalesperson;
    }

    public void setSalesperson(Boolean salesperson) {
        isSalesperson = salesperson;
    }

    @Basic
    @Column(name = "UserPreferences")
    public String getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }

    @Basic
    @Column(name = "PhoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Basic
    @Column(name = "FaxNumber")
    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    @Basic
    @Column(name = "EmailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Basic
    @Column(name = "Photo")
    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    @Basic
    @Column(name = "CustomFields")
    public String getCustomFields() {
        return customFields;
    }

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    @Basic
    @Column(name = "OtherLanguages")
    public String getOtherLanguages() {
        return otherLanguages;
    }

    public void setOtherLanguages(String otherLanguages) {
        this.otherLanguages = otherLanguages;
    }

    @Basic
    @Column(name = "LastEditedBy")
    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Basic
    @Column(name = "ValidFrom")
    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    @Basic
    @Column(name = "ValidTo")
    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(personId, person.personId) && Objects.equals(fullName, person.fullName) && Objects.equals(preferredName, person.preferredName) && Objects.equals(searchName, person.searchName) && Objects.equals(isPermittedToLogon, person.isPermittedToLogon) && Objects.equals(logonName, person.logonName) && Objects.equals(isExternalLogonProvider, person.isExternalLogonProvider) && Arrays.equals(hashedPassword, person.hashedPassword) && Objects.equals(isSystemUser, person.isSystemUser) && Objects.equals(isEmployee, person.isEmployee) && Objects.equals(isSalesperson, person.isSalesperson) && Objects.equals(userPreferences, person.userPreferences) && Objects.equals(phoneNumber, person.phoneNumber) && Objects.equals(faxNumber, person.faxNumber) && Objects.equals(emailAddress, person.emailAddress) && Arrays.equals(photo, person.photo) && Objects.equals(customFields, person.customFields) && Objects.equals(otherLanguages, person.otherLanguages) && Objects.equals(lastEditedBy, person.lastEditedBy) && Objects.equals(validFrom, person.validFrom) && Objects.equals(validTo, person.validTo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(personId, fullName, preferredName, searchName, isPermittedToLogon, logonName, isExternalLogonProvider, isSystemUser, isEmployee, isSalesperson, userPreferences, phoneNumber, faxNumber, emailAddress, customFields, otherLanguages, lastEditedBy, validFrom, validTo);
        result = 31 * result + Arrays.hashCode(hashedPassword);
        result = 31 * result + Arrays.hashCode(photo);
        return result;
    }
}
