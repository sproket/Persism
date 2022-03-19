package net.sf.persism.dao.so;

import java.sql.Date;

public class PostLink {
    private Integer id;
    private Date creationDate;
    private Integer postId;
    private Integer relatedPostId;
    private Integer linkTypeId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getRelatedPostId() {
        return relatedPostId;
    }

    public void setRelatedPostId(Integer relatedPostId) {
        this.relatedPostId = relatedPostId;
    }

    public Integer getLinkTypeId() {
        return linkTypeId;
    }

    public void setLinkTypeId(Integer linkTypeId) {
        this.linkTypeId = linkTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostLink postLinks = (PostLink) o;

        if (id != null ? !id.equals(postLinks.id) : postLinks.id != null) {
            return false;
        }
        if (creationDate != null ? !creationDate.equals(postLinks.creationDate) : postLinks.creationDate != null) {
            return false;
        }
        if (postId != null ? !postId.equals(postLinks.postId) : postLinks.postId != null) {
            return false;
        }
        if (relatedPostId != null ? !relatedPostId.equals(postLinks.relatedPostId) : postLinks.relatedPostId != null) {
            return false;
        }
        if (linkTypeId != null ? !linkTypeId.equals(postLinks.linkTypeId) : postLinks.linkTypeId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (postId != null ? postId.hashCode() : 0);
        result = 31 * result + (relatedPostId != null ? relatedPostId.hashCode() : 0);
        result = 31 * result + (linkTypeId != null ? linkTypeId.hashCode() : 0);
        return result;
    }
}
