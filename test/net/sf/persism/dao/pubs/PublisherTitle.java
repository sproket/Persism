package net.sf.persism.dao.pubs;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.Table;

import java.awt.image.TileObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Table("pub_info")
public class PublisherTitle {


    @Column(name = "pub_id")
    private String id;

    private byte[] logo;

    @Column(name = "pr_info")
    private String information;

    // @Join(to = Title.class, onProperties = "id", toProperties = "pubId")
    @Join(from = PublisherTitle.class, to = Title.class, onProperties = "id", toProperties = "pubId")
    @Join(from = TileObserver.class, to = Object.class, onProperties = "id", toProperties = "pubId")
    private final List<Title> titles = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public List<Title> getTitles() {
        return titles;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PublisherTitle{");
        sb.append("id='").append(id).append('\'');
//        sb.append(", logo=").append(Arrays.toString(logo));
//        sb.append(", information='").append(information).append('\'');
        sb.append(", titles=").append(titles);
        sb.append('}');
        return sb.toString();
    }
}
