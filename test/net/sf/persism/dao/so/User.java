package net.sf.persism.dao.so;

import java.sql.Timestamp;
import java.util.Objects;

public class User {
    private Integer id;
    private String aboutMe;
    private Integer age;
    private Timestamp creationDate;
    private String displayName;
    private Integer downVotes;
    private String emailHash;
    private Timestamp lastAccessDate;
    private String location;
    private Integer reputation;
    private Integer upVotes;
    private Integer views;
    private String websiteUrl;
    private Integer accountId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getDownVotes() {
        return downVotes;
    }

    public void setDownVotes(Integer downVotes) {
        this.downVotes = downVotes;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Timestamp getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Timestamp lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }

    public Integer getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(Integer upVotes) {
        this.upVotes = upVotes;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User users = (User) o;

        if (!Objects.equals(id, users.id)) {
            return false;
        }
        if (!Objects.equals(aboutMe, users.aboutMe)) {
            return false;
        }
        if (!Objects.equals(age, users.age)) {
            return false;
        }
        if (!Objects.equals(creationDate, users.creationDate)) {
            return false;
        }
        if (!Objects.equals(displayName, users.displayName)) {
            return false;
        }
        if (!Objects.equals(downVotes, users.downVotes)) {
            return false;
        }
        if (!Objects.equals(emailHash, users.emailHash)) {
            return false;
        }
        if (!Objects.equals(lastAccessDate, users.lastAccessDate)) {
            return false;
        }
        if (!Objects.equals(location, users.location)) {
            return false;
        }
        if (!Objects.equals(reputation, users.reputation)) {
            return false;
        }
        if (!Objects.equals(upVotes, users.upVotes)) {
            return false;
        }
        if (!Objects.equals(views, users.views)) {
            return false;
        }
        if (!Objects.equals(websiteUrl, users.websiteUrl)) {
            return false;
        }
        if (!Objects.equals(accountId, users.accountId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (aboutMe != null ? aboutMe.hashCode() : 0);
        result = 31 * result + (age != null ? age.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (downVotes != null ? downVotes.hashCode() : 0);
        result = 31 * result + (emailHash != null ? emailHash.hashCode() : 0);
        result = 31 * result + (lastAccessDate != null ? lastAccessDate.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (reputation != null ? reputation.hashCode() : 0);
        result = 31 * result + (upVotes != null ? upVotes.hashCode() : 0);
        result = 31 * result + (views != null ? views.hashCode() : 0);
        result = 31 * result + (websiteUrl != null ? websiteUrl.hashCode() : 0);
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
