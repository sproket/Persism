package net.sf.persism.dao.records;


import net.sf.persism.annotations.NotColumn;

import java.beans.ConstructorProperties;
import java.util.UUID;

public record RecordTest1(UUID id, String name, int qty, float price, @NotColumn double junk) {

    // Messed up constructor to test case where you put names but get the types wrong.
    // Should result in readRecord: Could instantiate the constructor for: class ...
    @ConstructorProperties({"id", "name", "qty", "price"})
    public RecordTest1(float id, UUID name, String qty, int price) {
        this(name, qty, price, id, 0.0d);
    }

}
