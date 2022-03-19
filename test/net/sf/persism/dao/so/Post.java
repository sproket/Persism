package net.sf.persism.dao.so;

import net.sf.persism.annotations.NotColumn;

import java.sql.Timestamp;
import java.util.Objects;

public final class Post {
    private Integer id;
    private Integer acceptedAnswerId;
    private int answerCount;
    private String body;
    private Timestamp closedDate;
    private int commentCount;
    private Timestamp communityOwnedDate;
    private Timestamp creationDate;
    private int favoriteCount;
    private Timestamp lastActivityDate;
    private Timestamp lastEditDate;
    private String lastEditorDisplayName;
    private Integer lastEditorUserId;
    private Integer ownerUserId;
    private Integer parentId;
    private Integer postTypeId;
    private int score;
    private String tags;
    private String title;
    private int viewCount;

    // this always is an infinite loop!
    //@Join(to = Post.class, onProperties = "parentId", toProperties = "id")
    @NotColumn
    private Post parentPost;


    public Post getParentPost() {
        return parentPost;
    }

    public void setParentPost(Post parentPost) {
        this.parentPost = parentPost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAcceptedAnswerId() {
        return acceptedAnswerId;
    }

    public void setAcceptedAnswerId(Integer acceptedAnswerId) {
        this.acceptedAnswerId = acceptedAnswerId;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }


    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getLastEditorDisplayName() {
        return lastEditorDisplayName;
    }

    public void setLastEditorDisplayName(String lastEditorDisplayName) {
        this.lastEditorDisplayName = lastEditorDisplayName;
    }

    public Integer getLastEditorUserId() {
        return lastEditorUserId;
    }

    public void setLastEditorUserId(Integer lastEditorUserId) {
        this.lastEditorUserId = lastEditorUserId;
    }

    public Integer getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getPostTypeId() {
        return postTypeId;
    }

    public void setPostTypeId(Integer postTypeId) {
        this.postTypeId = postTypeId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public Timestamp getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Timestamp closedDate) {
        this.closedDate = closedDate;
    }

    public Timestamp getCommunityOwnedDate() {
        return communityOwnedDate;
    }

    public void setCommunityOwnedDate(Timestamp communityOwnedDate) {
        this.communityOwnedDate = communityOwnedDate;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Timestamp getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(Timestamp lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public Timestamp getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(Timestamp lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return answerCount == post.answerCount && commentCount == post.commentCount && favoriteCount == post.favoriteCount && viewCount == post.viewCount && Objects.equals(id, post.id) && Objects.equals(acceptedAnswerId, post.acceptedAnswerId) && Objects.equals(body, post.body) && Objects.equals(closedDate, post.closedDate) && Objects.equals(communityOwnedDate, post.communityOwnedDate) && Objects.equals(creationDate, post.creationDate) && Objects.equals(lastActivityDate, post.lastActivityDate) && Objects.equals(lastEditDate, post.lastEditDate) && Objects.equals(lastEditorDisplayName, post.lastEditorDisplayName) && Objects.equals(lastEditorUserId, post.lastEditorUserId) && Objects.equals(ownerUserId, post.ownerUserId) && Objects.equals(parentId, post.parentId) && Objects.equals(postTypeId, post.postTypeId) && Objects.equals(score, post.score) && Objects.equals(tags, post.tags) && Objects.equals(title, post.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, acceptedAnswerId, answerCount, body, closedDate, commentCount, communityOwnedDate, creationDate, favoriteCount, lastActivityDate, lastEditDate, lastEditorDisplayName, lastEditorUserId, ownerUserId, parentId, postTypeId, score, tags, title, viewCount);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", creationDate=" + creationDate +
                ", parentId=" + parentId +
                '}';
    }
}
