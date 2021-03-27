package net.sf.persism.dao;

import net.sf.persism.PersistableObject;

import java.time.LocalDate;
import java.util.Date;

public final class Exam extends PersistableObject<Exam> {
    private int examId;
    private String accessionNo;
    private int patientNo;
    private LocalDate dateRequested;
    private String masterStatus;
    private int examStatusNo;
    private Date examDate;
    private int examCodeNo;
    private int roomNo;

    // Has
    private Object originalValue;

    public int getExamId() {
        return examId;
    }

    public String getAccessionNo() {
        return accessionNo;
    }

    public int getPatientNo() {
        return patientNo;
    }

    public LocalDate getDateRequested() {
        return dateRequested;
    }

    public String getMasterStatus() {
        return masterStatus;
    }

    public int getExamStatusNo() {
        return examStatusNo;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public void setAccessionNo(String accessionNo) {
        this.accessionNo = accessionNo;
    }

    public void setPatientNo(int patientNo) {
        this.patientNo = patientNo;
    }

    public void setDateRequested(LocalDate dateRequested) {
        this.dateRequested = dateRequested;
    }

    public void setMasterStatus(String masterStatus) {
        this.masterStatus = masterStatus;
    }

    public void setExamStatusNo(int examStatusNo) {
        this.examStatusNo = examStatusNo;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public int getExamCodeNo() {
        return examCodeNo;
    }

    public void setExamCodeNo(int examCodeNo) {
        this.examCodeNo = examCodeNo;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(Object originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "examId=" + examId +
                ", accessionNo='" + accessionNo + '\'' +
                ", patientNo=" + patientNo +
                ", examCodeNo=" + examCodeNo +
                ", roomNo=" + roomNo +
                ", dateRequested=" + dateRequested +
                ", masterStatus='" + masterStatus + '\'' +
                ", status=" + examStatusNo +
                ", examDate=" + examDate +
                '}';
    }
}
