package net.sf.persism;

import junit.framework.TestCase;

import java.sql.*;
import java.util.Properties;

// placeholder
public class TestInformix extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();


        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/informix.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
//        Class.forName(driver);

        url = "jdbc:informix-direct://pinf;user=pinf;password=pinf";
        Connection con = DriverManager.getConnection(url, "pinf", "pinf");

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSomething() throws Exception {
    }

}
