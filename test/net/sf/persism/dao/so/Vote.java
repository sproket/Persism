package net.sf.persism.dao.so;

import java.sql.Timestamp;
import java.util.Objects;

public final class Vote {

    private Integer id;
    private Integer postId;
    private Integer userId;
    private int bountyAmount;
    private Integer voteTypeId;
    private Timestamp creationDate;

    public Integer id() {
        return id;
    }

    public Vote setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer postId() {
        return postId;
    }

    public Vote setPostId(Integer postId) {
        this.postId = postId;
        return this;
    }

    public Integer userId() {
        return userId;
    }

    public Vote setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public int bountyAmount() {
        return bountyAmount;
    }

    public Vote setBountyAmount(int bountyAmount) {
        this.bountyAmount = bountyAmount;
        return this;
    }

    public Integer voteTypeId() {
        return voteTypeId;
    }

    public Vote setVoteTypeId(Integer voteTypeId) {
        this.voteTypeId = voteTypeId;
        return this;
    }

    public Timestamp creationDate() {
        return creationDate;
    }

    public Vote setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vote vote = (Vote) o;
        return bountyAmount == vote.bountyAmount && Objects.equals(id, vote.id) && Objects.equals(postId, vote.postId) && Objects.equals(userId, vote.userId) && Objects.equals(voteTypeId, vote.voteTypeId) && Objects.equals(creationDate, vote.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, postId, userId, bountyAmount, voteTypeId, creationDate);
    }

    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", bountyAmount=" + bountyAmount +
                ", voteTypeId=" + voteTypeId +
                ", creationDate=" + creationDate +
                '}';
    }
}
