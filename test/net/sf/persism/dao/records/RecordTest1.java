package net.sf.persism.dao.records;


import java.util.UUID;

// todo add qty, price and make total notcolumn?
public record RecordTest1(UUID id, String name, int something, double total ) {
}
