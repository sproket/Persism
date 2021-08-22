package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;

@Table("ROOMS")
public final class RoomMissingGetter {

    @Column(name = "Room_No")
    private int roomNo;

    @Column(name = "Desc_E")
    private String description;

    private BigDecimal intervals;

    private String weird;

    @NotColumn
    private String junk;

    public int getRoomNo() {
        return roomNo;
    }

    public String getDescription() {
        return description;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public void setDescription(String description) {
        this.description = description;
    }
// test for missing getter

//    public BigDecimal getIntervals() {
//        return intervals;
//    }

    public void setIntervals(BigDecimal intervals) {
        this.intervals = intervals;
    }

    public String getJunk() {
        return junk;
    }

    public String getWeird() {
        return weird;
    }

    public void setWeird(String weird) {
        this.weird = weird;
    }

    public void setJunk(String junk) {
        this.junk = junk;
    }
}
