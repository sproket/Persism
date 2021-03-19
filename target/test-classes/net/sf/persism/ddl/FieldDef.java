package net.sf.persism.ddl;

/**
 * Comments for FieldDef go here.
 *
 * @author Dan Howard
 * @since 10/22/11 6:29 AM
 */
public class FieldDef {

    private String name;
    private Class type;
    private int length;
    private int scale;

    private boolean primary;

    public FieldDef(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    public FieldDef(String name, Class type, int length, int scale) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.scale = scale;
    }

    public FieldDef(String name, Class type, boolean primary) {
        this.name = name;
        this.type = type;
        this.primary = primary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
