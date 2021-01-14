package net.sf.persism.dao;

import net.sf.persism.annotations.Query;

import java.util.Date;

/**
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/14/11
 * Time: 7:30 AM
 */
@Query
public class QueryResult {
    private int examId;
    private String procedureDescription;
    private String roomDescription;
    private Date examDate;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getProcedureDescription() {
        return procedureDescription;
    }

    public void setProcedureDescription(String procedureDescription) {
        this.procedureDescription = procedureDescription;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "examId=" + examId +
                ", procedureDescription='" + procedureDescription + '\'' +
                ", modalityDescription='" + roomDescription + '\'' +
                ", examDate=" + examDate +
                '}';
    }
}
