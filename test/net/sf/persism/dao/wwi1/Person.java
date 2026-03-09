package net.sf.persism.dao.wwi1;

import net.sf.persism.annotations.Table;

import java.util.Arrays;

@Table("People")
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

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public Boolean getPermittedToLogon() {
        return isPermittedToLogon;
    }

    public void setPermittedToLogon(Boolean permittedToLogon) {
        isPermittedToLogon = permittedToLogon;
    }

    public String getLogonName() {
        return logonName;
    }

    public void setLogonName(String logonName) {
        this.logonName = logonName;
    }

    public Boolean getExternalLogonProvider() {
        return isExternalLogonProvider;
    }

    public void setExternalLogonProvider(Boolean externalLogonProvider) {
        isExternalLogonProvider = externalLogonProvider;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Boolean getSystemUser() {
        return isSystemUser;
    }

    public void setSystemUser(Boolean systemUser) {
        isSystemUser = systemUser;
    }

    public Boolean getEmployee() {
        return isEmployee;
    }

    public void setEmployee(Boolean employee) {
        isEmployee = employee;
    }

    public Boolean getSalesperson() {
        return isSalesperson;
    }

    public void setSalesperson(Boolean salesperson) {
        isSalesperson = salesperson;
    }

    public String getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }


//    public JSONObject getUserPreferences() {
//        return userPreferences;
//    }
//
//    public void setUserPreferences(JSONObject userPreferences) {
//        this.userPreferences = userPreferences;
//    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getCustomFields() {
        return customFields;
    }

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    public String getOtherLanguages() {
        return otherLanguages;
    }

    public void setOtherLanguages(String otherLanguages) {
        this.otherLanguages = otherLanguages;
    }

    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    @Override
    public String toString() {
        return "Person{" +
               "personId=" + personId +
               ", fullName='" + fullName + '\'' +
               ", preferredName='" + preferredName + '\'' +
               ", searchName='" + searchName + '\'' +
               ", isPermittedToLogon=" + isPermittedToLogon +
               ", logonName='" + logonName + '\'' +
               ", isExternalLogonProvider=" + isExternalLogonProvider +
               ", hashedPassword=" + Arrays.toString(hashedPassword) +
               ", isSystemUser=" + isSystemUser +
               ", isEmployee=" + isEmployee +
               ", isSalesperson=" + isSalesperson +
               ", userPreferences='" + userPreferences + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", faxNumber='" + faxNumber + '\'' +
               ", emailAddress='" + emailAddress + '\'' +
               ", photo=" + Arrays.toString(photo) +
               ", customFields='" + customFields + '\'' +
               ", otherLanguages='" + otherLanguages + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
