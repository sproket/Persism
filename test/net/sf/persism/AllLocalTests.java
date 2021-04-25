package net.sf.persism;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.categories.LocalDB;
import net.sf.persism.categories.TestContainerDB;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

@Category(LocalDB.class)
public class AllLocalTests {

    private AllLocalTests() {
    }

    public static Test suite() throws Exception {

        TestSuite theTestSuite = new TestSuite();
        theTestSuite.addTestSuite(TestH2.class);
        theTestSuite.addTestSuite(TestHSQLDB.class);
        theTestSuite.addTestSuite(TestDerby.class);
        theTestSuite.addTestSuite(TestSQLite.class);
        theTestSuite.addTestSuite(TestMSAccess.class);

        return theTestSuite;
    }

}
