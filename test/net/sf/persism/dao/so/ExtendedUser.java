package net.sf.persism.dao.so;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;

import java.util.ArrayList;
import java.util.List;

@Table("Users")
public class ExtendedUser extends User {

    @Join(to = Vote.class, onProperties = "id", toProperties = "userId")
    List<Vote> votes = new ArrayList<>();

    @Join(to = ExtendedPost.class, onProperties = "id", toProperties = "ownerUserId")
    List<ExtendedPost> posts = new ArrayList<>();

    @Join(to = Badge.class, onProperties = "id", toProperties = "userId")
    List<Badge> badges = new ArrayList<>();

    transient List<String> otherStuff = new ArrayList<>();

    public List<Vote> getVotes() {
        return votes;
    }

    public List<ExtendedPost> getPosts() {
        return posts;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public List<String> getOtherStuff() {
        return otherStuff;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tExtendedUser{" +
                "votes=" + votes.size() +
                ", posts=" + posts.size() +
                ", badges=" + badges.size() +
                '}';
    }
}
