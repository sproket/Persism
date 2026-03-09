package net.sf.persism.dao.wwi1;

// watre
public final class Color {
    private Integer colorId;
    private String colorName;
    private Integer lastEditedBy;

    public Integer colorId() {
        return colorId;
    }

    public Color setColorId(Integer colorId) {
        this.colorId = colorId;
        return this;
    }

    public String colorName() {
        return colorName;
    }

    public Color setColorName(String colorName) {
        this.colorName = colorName;
        return this;
    }

    public Integer lastEditedBy() {
        return lastEditedBy;
    }

    public Color setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Color color = (Color) o;

        if (colorId != null ? !colorId.equals(color.colorId) : color.colorId != null) {
            return false;
        }
        if (colorName != null ? !colorName.equals(color.colorName) : color.colorName != null) {
            return false;
        }
        return lastEditedBy != null ? lastEditedBy.equals(color.lastEditedBy) : color.lastEditedBy == null;
    }

    @Override
    public int hashCode() {
        int result = colorId != null ? colorId.hashCode() : 0;
        result = 31 * result + (colorName != null ? colorName.hashCode() : 0);
        result = 31 * result + (lastEditedBy != null ? lastEditedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Color{" +
               "colorId=" + colorId +
               ", colorName='" + colorName + '\'' +
               ", lastEditedBy=" + lastEditedBy +
               '}';
    }
}
