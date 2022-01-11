package net.sf.persism.dao.so;

import net.sf.persism.annotations.Join;

import java.util.ArrayList;
import java.util.List;

// TODO THIS CLASS WAS ADDED BY ME to test multi column join
public final class UserCommentXref  {

    private Integer id;
    private Integer userId; // could be NULL
    private Integer postId;

    @Join(to = Comment.class, onProperties = "userId, postId", toProperties = "userId, postId")
    private List<Comment> comments = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
