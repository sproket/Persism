package net.sf.persism.dao.records;


import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotColumn;

import java.beans.ConstructorProperties;
import java.util.UUID;

// Designed for negative testing. This will fail because of the additional constructor which is messed up
// AND
// The canonical constructor has an extra "junk" so it won't be found since "junk" is not in the table.
public record RecordTest1(@Column(primary = true) UUID id, String name, int qty, float price, @NotColumn double junk) {

    // Messed up constructor to test case where you put names but get the types wrong.
    // Should result in readRecord: Could instantiate the constructor for: class ...
    @ConstructorProperties({"id", "name", "qty", "price"})
    public RecordTest1(float id, UUID name, String qty, int price) {
        this(name, qty, price, id, 0.0d);
    }

}
