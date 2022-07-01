package net.sf.persism.dao.so;

import net.sf.persism.annotations.Join;

import java.sql.Timestamp;

public final class Comment {
    private Integer id;
    private Timestamp creationDate;
    private Integer postId;
    private Integer score;
    private String text;
    private Integer userId;

    @Join(to = User.class, onProperties = "userId", toProperties = "id")
    private User user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Comment comments = (Comment) o;

        if (id != null ? !id.equals(comments.id) : comments.id != null) {
            return false;
        }
        if (creationDate != null ? !creationDate.equals(comments.creationDate) : comments.creationDate != null) {
            return false;
        }
        if (postId != null ? !postId.equals(comments.postId) : comments.postId != null) {
            return false;
        }
        if (score != null ? !score.equals(comments.score) : comments.score != null) {
            return false;
        }
        if (text != null ? !text.equals(comments.text) : comments.text != null) {
            return false;
        }
        if (userId != null ? !userId.equals(comments.userId) : comments.userId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (postId != null ? postId.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);

        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", postId=" + postId +
                ", score=" + score +
                ", userId=" + userId +
                '}';
    }
}
