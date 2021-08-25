package net.sf.persism.dao.records;

// TODO import net.sf.persism.annotations.NotColumn;
import net.sf.persism.TestDescription;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;

import java.sql.Time;

// Some support for extra constructors (using  @ConstructorProperties)
// See this. Word from the TOP. ;)
// https://stackoverflow.com/questions/67038058/record-cannot-get-parameter-names-from-constructors/67048729

// if the BaseTest::testQueryResultRecordNegative fails over this it probably means we compiled with -parameters (NEVER DEPEND ON THAT)

// used for failing
@NotTable
public record CustomerOrderGarbage(String customerId,
                                    @NotColumn String junk, // java: annotation type not applicable to this kind of declaration - no fail in Java 16.
                                    @NotColumn int missing,
                                    @NotColumn Time whatTimeIsIt) {

    // todo test @Column as well.

    private static int extraFieldShouldBeIgnoredBecauseStatic;

    @TestDescription("Canonical")
    public CustomerOrderGarbage {
    }

    // WTF extra - statics ignored...
    @TestDescription("String customerId, String junk, int missing, Time whatTimeIsIt, int extraFieldShouldBeIgnoredBecauseStatic")
    public CustomerOrderGarbage(String customerId, String junk, int missing, Time whatTimeIsIt, int extraFieldShouldBeIgnoredBecauseStatic) {
        this(customerId, junk, missing, whatTimeIsIt);
        this.extraFieldShouldBeIgnoredBecauseStatic = extraFieldShouldBeIgnoredBecauseStatic;
    }

    public static int getExtraFieldShouldBeIgnoredBecauseStatic() {
        return extraFieldShouldBeIgnoredBecauseStatic;
    }

    // Selecting for only this works as long as we have a constructor
    @TestDescription("custid only")
    public CustomerOrderGarbage(String customerId) {
        this(customerId, null, 0, null);
    }
}
