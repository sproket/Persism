package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.util.Properties;

// placeholder
@Category(ExternalDB.class)
public class TestInformix extends TestCase {

    private static final Log log = Log.getLogger(TestInformix.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();


        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/informix.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        log.warn(driver);

        log.warn(url);
//        url = "jdbc:informix-direct://pinf;user=pinf;password=pinf";
        Connection con = DriverManager.getConnection(url, username, password);

        Session session = new Session(con, "SOME KEY");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSomething() throws Exception {
    }

}
