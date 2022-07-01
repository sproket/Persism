package net.sf.persism.dao;

import net.sf.persism.annotations.Table;

@Table("People")
public class Person {
    private int id;
    private String name;
    private int motherId;
    private int fatherId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMotherId() {
        return motherId;
    }

    public void setMotherId(int motherId) {
        this.motherId = motherId;
    }

    public int getFatherId() {
        return fatherId;
    }

    public void setFatherId(int fatherId) {
        this.fatherId = fatherId;
    }

    @Override
    public String toString() {
        return "Person{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", motherId=" + motherId +
               ", fatherId=" + fatherId +
               '}';
    }
}
