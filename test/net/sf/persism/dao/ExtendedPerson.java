package net.sf.persism.dao;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.Table;

import java.util.List;

@Table("People")
public class ExtendedPerson extends Person {

    @Join(to = Person.class, onProperties = "motherId", toProperties = "id")
    private Person mother;

    @Join(to = Person.class, onProperties = "fatherId", toProperties = "id")
    private Person father;

    @NotColumn
    private List<ExtendedPerson> siblings;

    @NotColumn
    private List<ExtendedPerson> cousins;

    public Person getMother() {
        return mother;
    }

    public Person getFather() {
        return father;
    }

}
