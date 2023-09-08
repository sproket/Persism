package net.sf.persism.dao;

import net.sf.persism.annotations.Table;

import java.sql.Date;
import java.util.UUID;
// This version has an extra field to fail a unit test and exercise the exception
@Table("Contacts")
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

    private boolean fail; // extra property to fail since it's not in the DB and not marked @NotColumn

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

//    public String getLastname() {
//        return lastname;
//    }

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

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContactFail contact = (ContactFail) o;

        if (address1 != null ? !address1.equals(contact.address1) : contact.address1 != null) {
            return false;
        }
        if (address2 != null ? !address2.equals(contact.address2) : contact.address2 != null) {
            return false;
        }
        if (city != null ? !city.equals(contact.city) : contact.city != null) {
            return false;
        }
        if (company != null ? !company.equals(contact.company) : contact.company != null) {
            return false;
        }
        if (contactName != null ? !contactName.equals(contact.contactName) : contact.contactName != null) {
            return false;
        }
        if (country != null ? !country.equals(contact.country) : contact.country != null) {
            return false;
        }
        if (dateAdded != null ? !dateAdded.equals(contact.dateAdded) : contact.dateAdded != null) {
            return false;
        }
        if (division != null ? !division.equals(contact.division) : contact.division != null) {
            return false;
        }
        if (email != null ? !email.equals(contact.email) : contact.email != null) {
            return false;
        }
        if (firstname != null ? !firstname.equals(contact.firstname) : contact.firstname != null) {
            return false;
        }
        if (identity != null ? !identity.equals(contact.identity) : contact.identity != null) {
            return false;
        }
        if (lastModified != null ? !lastModified.equals(contact.lastModified) : contact.lastModified != null) {
            return false;
        }
        if (lastname != null ? !lastname.equals(contact.lastname) : contact.lastname != null) {
            return false;
        }
        if (partnerId != null ? !partnerId.equals(contact.partnerId) : contact.partnerId != null) {
            return false;
        }
        if (stateProvince != null ? !stateProvince.equals(contact.stateProvince) : contact.stateProvince != null) {
            return false;
        }
        if (type != null ? !type.equals(contact.type) : contact.type != null) {
            return false;
        }
        if (zipPostalCode != null ? !zipPostalCode.equals(contact.zipPostalCode) : contact.zipPostalCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = identity != null ? identity.hashCode() : 0;
        result = 31 * result + (partnerId != null ? partnerId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
        result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 31 * result + (contactName != null ? contactName.hashCode() : 0);
        result = 31 * result + (company != null ? company.hashCode() : 0);
        result = 31 * result + (division != null ? division.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (stateProvince != null ? stateProvince.hashCode() : 0);
        result = 31 * result + (zipPostalCode != null ? zipPostalCode.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (dateAdded != null ? dateAdded.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        return result;
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
