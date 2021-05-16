package net.sf.persism.dao;

//public class Product  {
//    private final int id;
//    private final String description;
//    private final double cost;
//
//    public Product(int id, String description, double cost) {
//        this.id = id;
//        this.description = description;
//        this.cost = cost;
//    }
//
//    public int id() {
//        return id;
//    }
//
//    public String description() {
//        return description;
//    }
//
//    public double cost() {
//        return cost;
//    }
//}

import java.util.UUID;


// TODO WTF happens in this case? desciption in the main constructor?
public record Product(int id, String desciption, double cost, UUID junk) {

    // for test multiple constructors. We only look at the main constructor and expect all columns. for now
    public Product(int id, String description) {
        this(id, description, 0, null);
    }
}
