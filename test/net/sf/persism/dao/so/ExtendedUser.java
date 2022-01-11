package net.sf.persism.dao.so;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.Table;

import java.util.ArrayList;
import java.util.List;

@Table("Users")
public class ExtendedUser extends User {

    //@Join(to = Vote.class, onProperties = "id", toProperties = "userId")
    @NotColumn
    List<Vote> votes = new ArrayList<>();

    @Join(to = Post.class, onProperties = "id", toProperties = "ownerUserId")
    List<Post> posts = new ArrayList<>();

//    @Join(to = Badge.class, onProperties = "id", toProperties = "userId")
    transient List<Badge> badges = new ArrayList<>();

    public List<Vote> getVotes() {
        return votes;
    }
    public List<Post> getPosts() {
        return posts;
    }
    public List<Badge> getBadges() {
        return badges;
    }

    @Override
    public String toString() {
        return super.toString() + "\nExtendedUser{" +
                "votes=" + votes +
                ", posts=" + posts +
                ", badges=" + badges +
                '}';
    }
}
