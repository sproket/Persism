package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.northwind.Category;

import java.sql.*;
import java.util.List;
import java.util.Properties;

public class TestMiscellaneous extends TestCase {

    private static final Log log = Log.getLogger(TestMiscellaneous.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAutoClosable() throws Exception {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/northwind.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        Connection con = DriverManager.getConnection(url, username, password);

        try (Session session = new Session(con)) {
            List<Category> list = session.query(Category.class, "select * from categories");
            list.stream().forEach(c -> log.info(c));
        }
        assertTrue(con.isClosed());
    }

    public static void testSomething() {
        //Timestamp
        String v1 = "1994-02-17 10:23:43.9970000";
        String v2 = "1994-02-17 10:23:43.997";
        String v3 = "1994-02-17 10:23:43";
        String v4 = "1994-02-17";


        log.warn(Timestamp.valueOf(v1));
        log.warn(Timestamp.valueOf(v2));
        log.warn(Timestamp.valueOf(v3));
        try {
            log.warn(Timestamp.valueOf(v4));
        } catch (IllegalArgumentException e) {
            log.info(e);
        }
    }
}
