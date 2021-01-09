package net.sf.persism;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Comments for AllTests go here.
 *
 * @author Dan Howard
 * @since 10/8/11 6:03 PM
 */
public class AllTests {

    private AllTests() {
    }

    public static Test suite() throws Exception {

        TestSuite theTestSuite = new TestSuite();

        // Order is important TestMSSQL is listed twice and TestOracle switches modes from mssql to jtds for the 2nd instance
        theTestSuite.addTestSuite(TestH2.class);
        theTestSuite.addTestSuite(TestMSSQL.class);
        theTestSuite.addTestSuite(TestOracle.class);
        theTestSuite.addTestSuite(TestMSSQL.class);
        theTestSuite.addTestSuite(TestSQLite.class);
        theTestSuite.addTestSuite(TestDerby.class);
        theTestSuite.addTestSuite(TestMySQL.class);
        theTestSuite.addTestSuite(TestPostgreSQL.class);

        theTestSuite.addTestSuite(TestTypes.class);
        theTestSuite.addTestSuite(TestNorthwind.class);
        theTestSuite.addTestSuite(TestPubs.class);
        theTestSuite.addTestSuite(TestMiscellaneous.class);


        return theTestSuite;
    }

}
