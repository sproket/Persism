package net.sf.persism.dao;

import java.time.LocalDate;
import java.util.Date;

public final class Exam {
    private int examId;
    private String accessionNo;
    private int patientNo;
    private LocalDate dateRequested;
    private String masterStatus;
    private int status;
    private Date examDate;
    private int examCodeNo;
    private int roomNo;

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

    public int getStatus() {
        return status;
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

    public void setStatus(int status) {
        this.status = status;
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
                ", status=" + status +
                ", examDate=" + examDate +
                '}';
    }
}
