package net.sf.persism.dao.records;

import java.time.LocalDateTime;

public record RecordTest2(int id, int something, double total, LocalDateTime createdOn) {

    public RecordTest2(int id, int something, double total) {
        this(id, something, total, null);
    }
}
