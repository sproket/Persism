package net.sf.persism.dao;

public record Product(int id, String description, double cost) {

    // for test multiple constructors. We only look at the main constructor and expect all columns. for now
    public Product(int id, String description) {
        this(id, description, 0);
    }
}
