package net.sf.persism.dao;

import net.sf.persism.PersistableObject;

import java.sql.Time;
import java.util.Date;

/**
 * Comments for SavedGame go here.
 *
 * @author Dan Howard
 * @since 3/30/13 7:59 AM
 */
public final class SavedGame extends PersistableObject<SavedGame> {

    private int id;
    private String name;
    private Date someDateAndTime;
    private String data;
    private float gold;
    private int silver;
    private long copper;
    private byte[] somethingBig;
    private Time whatTimeIsIt;

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

    public Date getSomeDateAndTime() {
        return someDateAndTime;
    }

    public void setSomeDateAndTime(Date someDateAndTime) {
        this.someDateAndTime = someDateAndTime;
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

    public Time getWhatTimeIsIt() {
        return whatTimeIsIt;
    }

    public void setWhatTimeIsIt(Time whatTimeIsIt) {
        this.whatTimeIsIt = whatTimeIsIt;
    }

    public long getCopper() {
        return copper;
    }

    public void setCopper(long copper) {
        this.copper = copper;
    }
}
