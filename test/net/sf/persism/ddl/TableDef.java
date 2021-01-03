package net.sf.persism.ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comments for Table go here.
 *
 * @author Dan Howard
 * @since 10/22/11 6:29 AM
 */
public final class TableDef {

    private String name;
    private FieldDef primary;
    private List<FieldDef> fields;

    public TableDef() {
        name = "";
        primary = null;
        fields = new ArrayList<FieldDef>(32);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldDef getPrimary() {
        return primary;
    }

    public void setPrimary(FieldDef primary) {
        this.primary = primary;
    }

    public void addField(FieldDef field) {
        fields.add(field);
    }

    public List<FieldDef> getFields() {
        return Collections.unmodifiableList(fields);
    }
}
