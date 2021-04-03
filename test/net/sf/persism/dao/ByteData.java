package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotColumn;

public final class ByteData {
    @Column(primary = true)
    private Character id;

    private byte byte1;
    private short byte2;

    @NotColumn
    private int somethingInt;
    @NotColumn
    private short somethingShort;

    public Character getId() {
        return id;
    }

    public void setId(Character id) {
        this.id = id;
    }

    public byte getByte1() {
        return byte1;
    }

    public void setByte1(byte byte1) {
        this.byte1 = byte1;
    }

    public short getByte2() {
        return byte2;
    }

    public void setByte2(short byte2) {
        this.byte2 = byte2;
    }

    public int getSomethingInt() {
        return somethingInt;
    }

    public void setSomethingInt(int somethingInt) {
        this.somethingInt = somethingInt;
    }

    public short getSomethingShort() {
        return somethingShort;
    }

    public void setSomethingShort(short somethingShort) {
        this.somethingShort = somethingShort;
    }

    @Override
    public String toString() {
        return "ByteData{" +
                "id='" + id + '\'' +
                ", byte1=" + byte1 +
                ", byte2=" + byte2 +
                '}';
    }
}
