package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

import java.math.BigDecimal;

/**
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 8:04 AM
 */
public final class Room {

    @Column(value = "Room_No")
    private int roomNo;

    @Column(value = "Desc_E")
    private String description;

    private BigDecimal intervals;

    // //@NoColumn
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

    public BigDecimal getIntervals() {
        return intervals;
    }

    public void setIntervals(BigDecimal intervals) {
        this.intervals = intervals;
    }

    public String getJunk() {
        return junk;
    }
}
