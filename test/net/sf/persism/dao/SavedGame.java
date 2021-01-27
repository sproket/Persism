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
    private float gold;
    private int silver;
    private byte[] somethingBig;

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

    public byte[] getSomethingBig() {
        return somethingBig;
    }

    public void setSomethingBig(byte[] somethingBig) {
        this.somethingBig = somethingBig;
    }

    public void setData(String data) {
        this.data = data;
    }

    public float getGold() {
        return gold;
    }

    public void setGold(float gold) {
        this.gold = gold;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }
}
