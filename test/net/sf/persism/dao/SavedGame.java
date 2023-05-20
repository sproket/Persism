package net.sf.persism.dao;

import net.sf.persism.PersistableObject;

import java.sql.Time;
import java.util.Arrays;
import java.util.Date;

/**
 * Comments for SavedGame go here.
 *
 * @author Dan Howard
 * @since 3/30/13 7:59 AM
 */
public final class SavedGame extends PersistableObject<SavedGame> {

    // Changed to String to see if this fails as an autoinc. In H2 it still works. Other DBs it fails (as it should). IT DOES FAIL IN H2 NOW!
    private String id;
    private String name;
    private Date someDateAndTime;
    private String data;
    private double platinum;
    private float gold;
    private int silver;
    private long copper;
    private byte[] somethingBig;
    private Time whatTimeIsIt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public double platinum() {
        return platinum;
    }

    public void setPlatinum(double platinum) {
        this.platinum = platinum;
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

    @Override
    public String toString() {
        return "\nSavedGame{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", someDateAndTime=" + someDateAndTime +
               ", data='" + data + '\'' +
               ", gold=" + gold +
               ", silver=" + silver +
               ", copper=" + copper +
               ", somethingBig=" + Arrays.toString(somethingBig) +
               ", whatTimeIsIt=" + whatTimeIsIt +
               '}';
    }
}
