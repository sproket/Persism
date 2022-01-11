package net.sf.persism.dao.so;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public final class Vote {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private Integer bountyAmount;
    private Integer voteTypeId;
    private Timestamp creationDate;

    public Vote() {
    }

    public Vote(Integer id, Integer postId, Integer userId, Integer bountyAmount, Integer voteTypeId, Timestamp creationDate) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.bountyAmount = bountyAmount;
        this.voteTypeId = voteTypeId;
        this.creationDate = creationDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBountyAmount() {
        return bountyAmount;
    }

    public void setBountyAmount(Integer bountyAmount) {
        this.bountyAmount = bountyAmount;
    }

    public Integer getVoteTypeId() {
        return voteTypeId;
    }

    public void setVoteTypeId(Integer voteTypeId) {
        this.voteTypeId = voteTypeId;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vote votes = (Vote) o;

        if (id != null ? !id.equals(votes.id) : votes.id != null) {
            return false;
        }
        if (postId != null ? !postId.equals(votes.postId) : votes.postId != null) {
            return false;
        }
        if (userId != null ? !userId.equals(votes.userId) : votes.userId != null) {
            return false;
        }
        if (bountyAmount != null ? !bountyAmount.equals(votes.bountyAmount) : votes.bountyAmount != null) {
            return false;
        }
        if (voteTypeId != null ? !voteTypeId.equals(votes.voteTypeId) : votes.voteTypeId != null) {
            return false;
        }
        if (creationDate != null ? !creationDate.equals(votes.creationDate) : votes.creationDate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (postId != null ? postId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (bountyAmount != null ? bountyAmount.hashCode() : 0);
        result = 31 * result + (voteTypeId != null ? voteTypeId.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
    }
}
