package net.sf.persism.dao.pubs;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.util.Arrays;

@Table("pub_info")
public final class PublisherInfo {

    @Column(name = "pub_id")
    private String id;

    private byte[] logo;

    @Column(name = "pr_info")
    private String information;

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

    @Override
    public String toString() {
        return "PublisherInfo{" +
                "id='" + id + '\'' +
                ", information='" + information + '\'' +
                ", logo=" + Arrays.toString(logo) +
                '}';
    }
}
