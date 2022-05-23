package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.wwi1.Application;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@Category(ExternalDB.class)

public final class TestWideWorldImporters extends TestCase {

    private static final Log log = Log.getLogger(TestWideWorldImporters.class);

    Connection con;
    Session session;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // this test is in case we use TestWideWorldImportersContainer
        //noinspection ConstantConditions
        if (getClass().equals(TestWideWorldImporters.class)) {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/wwi.properties"));
            String driver = props.getProperty("database.driver");
            String url = props.getProperty("database.url");
            String username = props.getProperty("database.username");
            String password = props.getProperty("database.password");
            Class.forName(driver);

            con = DriverManager.getConnection(url, username, password);

            session = new Session(con);
        }
    }

    public void testAnything() {
        try {
            session.query(Application.City.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }
}
