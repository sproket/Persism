package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

//import java.sql.Timestamp;
import java.sql.Timestamp;
import java.util.Date;

public final class User {
    @Column(name = "User_No", autoIncrement = true, primary = true)
    private long id;

    @Column(name = "UserCode")
    private String userName;

    private String name;

    private String status;

    private String typeOfUser;

    private int department;

    private Date lastLogin;

    private Timestamp someDate;

    // Money tests
    private double amountOwed;
    private float amountOwedAfterHeadRemoval;

    public Timestamp getSomeDate() {
        return someDate;
    }

    public void setSomeDate(Timestamp someDate) {
        this.someDate = someDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeOfUser() {
        return typeOfUser;
    }

    public void setTypeOfUser(String typeOfUser) {
        this.typeOfUser = typeOfUser;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public double getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(double amountOwed) {
        this.amountOwed = amountOwed;
    }

    public float getAmountOwedAfterHeadRemoval() {
        return amountOwedAfterHeadRemoval;
    }

    public void setAmountOwedAfterHeadRemoval(float amountOwedAfterHeadRemoval) {
        this.amountOwedAfterHeadRemoval = amountOwedAfterHeadRemoval;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", typeOfUser='" + typeOfUser + '\'' +
                ", department=" + department +
                ", lastLogin=" + lastLogin +
                ", someDate=" + someDate +
                ", amountOwed=" + amountOwed +
                ", amountOwedAfterHeadRemoval=" + amountOwedAfterHeadRemoval +
                '}';
    }
}
