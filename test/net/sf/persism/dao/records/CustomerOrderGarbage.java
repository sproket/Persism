package net.sf.persism.dao.records;

// TODO import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;

import java.sql.Time;

// No support for extra constructors
// See this. Word from the TOP. ;)
// https://stackoverflow.com/questions/67038058/record-cannot-get-parameter-names-from-constructors/67048729

// used for failing
@NotTable
public record CustomerOrderGarbage(String customerId,
                                    String junk, // java: annotation type not applicable to this kind of declaration THIS ONLY FAILS IF I HAVE A COMPACT CONSTRUCTOR? WTF!
                                     int missing,
                                     Time whatTimeIsIt) {

    // todo test @Column as well.

    private static int extraFieldShouldBeIgnoredBecauseStatic;

    public CustomerOrderGarbage {
    }

    // WTF ASS?
    public CustomerOrderGarbage(String customerId, String junk, int missing, Time whatTimeIsIt, int extraFieldShouldBeIgnoredBecauseStatic) {
        this(customerId, junk, missing, whatTimeIsIt);
        this.extraFieldShouldBeIgnoredBecauseStatic = extraFieldShouldBeIgnoredBecauseStatic;
    }

    public static int getExtraFieldShouldBeIgnoredBecauseStatic() {
        return extraFieldShouldBeIgnoredBecauseStatic;
    }

    // Selecting for only this works as long as we have a constructor
    public CustomerOrderGarbage(String customerId) {
        this(customerId, null, 0, null);
    }
//
//    public CustomerOrderGarbage(String customerId, String junk) {
//        this(customerId, junk, 0, null);
//    }
}
