package net.sf.persism.dao;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Join;

import java.util.ArrayList;
import java.util.List;

public final class CustomerGroup {
    @Column(primary = true)
    private int groupId;
    private String groupName;

    @Join(to = Customer.class, onProperties = "groupId", toProperties = "groupId")
    private List<Customer> customers = new ArrayList<>();

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    @Override
    public String toString() {
        return "CustomerGroup{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
