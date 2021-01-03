package net.sf.persism.dao;

import net.sf.persism.PersistableObject;

import java.util.Date;

/**
 * Comments for SavedGame go here.
 *
 * @author Dan Howard
 * @since 3/30/13 7:59 AM
 */
public final class SavedGame extends PersistableObject {

    private int id;
    private String name;
    private Date timeStamp;
    private String data;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
