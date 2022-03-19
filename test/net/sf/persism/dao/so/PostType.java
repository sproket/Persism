package net.sf.persism.dao.so;

public class PostType {
    private Integer id;
    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostType postTypes = (PostType) o;

        if (id != null ? !id.equals(postTypes.id) : postTypes.id != null) {
            return false;
        }
        if (type != null ? !type.equals(postTypes.type) : postTypes.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PostType{" +
                "id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}
