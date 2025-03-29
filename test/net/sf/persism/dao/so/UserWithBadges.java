package net.sf.persism.dao.so;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.Table;

import java.util.ArrayList;
import java.util.List;

@Table("Users")
public class UserWithBadges extends User {

    @Join(to= Badge.class, onProperties = "id", toProperties = "userId")
    private List<Badge> badges = new ArrayList<>();

    public List<Badge> getBadges() {
        return badges;
    }
}
