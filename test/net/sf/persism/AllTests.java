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

        theTestSuite.addTestSuite(TestH2.class);
        theTestSuite.addTestSuite(TestMSSQL.class);
        theTestSuite.addTestSuite(TestOracle.class);
        theTestSuite.addTestSuite(TestSQLite.class);
        theTestSuite.addTestSuite(TestDerby.class);
        theTestSuite.addTestSuite(TestMySQL.class);
        theTestSuite.addTestSuite(TestPostgreSQL.class);

        theTestSuite.addTestSuite(TestTypes.class);
        theTestSuite.addTestSuite(TestNorthwind.class);
        theTestSuite.addTestSuite(TestPubs.class);


        return theTestSuite;
    }

}
