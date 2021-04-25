package net.sf.persism.dao.records;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;

/**
 * test for calculated field
 * test for 2nd constructor with a missing property. Doesn't really have any effect.
 * The 2nd is just a convenience constructor.
 */
public record RecordTest2(int id, String description, int qty, double price, LocalDateTime createdOn) {

    // calculated field
    public double total() {
        return price * qty;
    }

    @ConstructorProperties({"id", "description", "qty", "price"})
    public RecordTest2(int id, String description, int qty, double price) {
        this(id, description, qty, price, LocalDateTime.now());
    }
}
