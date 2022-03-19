package net.sf.persism.dao.so;

public class VoteType {
    private Integer id;
    private String name;

    public VoteType() {
    }

    public VoteType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer id() {
        return id;
    }

    public VoteType id(Integer id) {
        this.id = id;
        return this;
    }

    public String name() {
        return name;
    }

    public VoteType name(String name) {
        this.name = name;
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

        VoteType voteTypes = (VoteType) o;

        if (id != null ? !id.equals(voteTypes.id) : voteTypes.id != null) {
            return false;
        }
        if (name != null ? !name.equals(voteTypes.name) : voteTypes.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VoteType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
