package net.sf.persism.dao;

import net.sf.persism.PersistableObject;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotMapped;
import net.sf.persism.annotations.TableName;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Example extending PersistableObject
 * User: DHoward
 * Date: 9/8/11
 * Time: 6:16 AM
 */
@TableName("EXAMCODE")
public final class Procedure extends PersistableObject<Procedure> {

    @Column("ExamCode_No")
    private int examCodeNo;

    @Column("Desc_E")
    private String description;

    @Column("ExamType_No")
    private int modalityId;

    private String professionalFeeCode;
    private String technicalFeeCode;

    @Column(value = "AccompanyingExamCode_No", primary = false)
    private int accompanyingProcedureId;

    private BigDecimal points;

    private boolean sideRequired;

    private String allowInRooms;

    private int bodyPartNo;

    private String prepInstructions;

    private int reservationType;

    @NotMapped
    private Date someDate;

    public int getExamCodeNo() {
        return examCodeNo;
    }

    public void setExamCodeNo(int examCodeNo) {
        this.examCodeNo = examCodeNo;
    }

    public String getProfessionalFeeCode() {
        return professionalFeeCode;
    }

    public void setProfessionalFeeCode(String professionalFeeCode) {
        this.professionalFeeCode = professionalFeeCode;
    }

    public String getTechnicalFeeCode() {
        return technicalFeeCode;
    }

    public void setTechnicalFeeCode(String technicalFeeCode) {
        this.technicalFeeCode = technicalFeeCode;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public boolean isSideRequired() {
        return sideRequired;
    }

    public void setSideRequired(boolean sideRequired) {
        this.sideRequired = sideRequired;
    }

    public String getAllowInRooms() {
        return allowInRooms;
    }

    public void setAllowInRooms(String allowInRooms) {
        this.allowInRooms = allowInRooms;
    }

    public int getBodyPartNo() {
        return bodyPartNo;
    }

    public void setBodyPartNo(int bodyPartNo) {
        this.bodyPartNo = bodyPartNo;
    }

    public String getPrepInstructions() {
        return prepInstructions;
    }

    public void setPrepInstructions(String prepInstructions) {
        this.prepInstructions = prepInstructions;
    }

    public int getReservationType() {
        return reservationType;
    }

    public void setReservationType(int reservationType) {
        this.reservationType = reservationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getModalityId() {
        return modalityId;
    }

    public void setModalityId(int modalityId) {
        this.modalityId = modalityId;
    }

    public int getAccompanyingProcedureId() {
        return accompanyingProcedureId;
    }

    public void setAccompanyingProcedureId(int accompanyingProcedureId) {
        this.accompanyingProcedureId = accompanyingProcedureId;
    }

    public Date getSomeDate() {
        return someDate;
    }

    public void setSomeDate(Date someDate) {
        this.someDate = someDate;
    }

    @Override
    public String toString() {
        return "Procedure{" +
                "examCodeNo=" + examCodeNo +
                ", description='" + description + '\'' +
                '}';
    }
}
