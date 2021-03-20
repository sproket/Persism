package net.sf.persism;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.categories.TestContainerDB;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Comments for AllTests go here.
 *
 * @author Dan Howard
 * @since 10/8/11 6:03 PM
 */
@Category({TestContainerDB.class, ExternalDB.class})
@Ignore
public class AllTests {

    private AllTests() {
    }

    public static Test suite() throws Exception {

        TestSuite theTestSuite = new TestSuite();

        // Order is important TestMSSQL is listed twice and TestOracle switches modes from mssql to jtds for the 2nd instance
        theTestSuite.addTestSuite(TestH2.class);
        theTestSuite.addTestSuite(TestHSQLDB.class);
        theTestSuite.addTestSuite(TestDerby.class);
        theTestSuite.addTestSuite(TestSQLite.class);
        theTestSuite.addTestSuite(TestMSAccess.class);

        theTestSuite.addTestSuite(TestMSSQL.class);
        theTestSuite.addTestSuite(TestOracle.class);
        theTestSuite.addTestSuite(TestMSSQL.class);
        theTestSuite.addTestSuite(TestMySQL.class);
        theTestSuite.addTestSuite(TestPostgreSQL.class);
        theTestSuite.addTestSuite(TestFirebird.class);

        theTestSuite.addTestSuite(TestMetaData.class);
        theTestSuite.addTestSuite(TestNorthwind.class);
        theTestSuite.addTestSuite(TestPubs.class);

        return theTestSuite;
    }

}
