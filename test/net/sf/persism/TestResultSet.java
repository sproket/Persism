/**
 * Comments for TestResultSet go here.
 * @author Dan Howard
 * @since 6/20/12 7:53 PM
 */
package net.sf.persism;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestResultSet extends TestCase {

    private static final Log log = Log.getLogger(TestResultSet.class);


    Connection con;
    Query query;
    Command command;


    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/northwind.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        query = new Query(con);
        command = new Command(con);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
[CustomerID] [nchar](5) NOT NULL,
	[CompanyName] [nvarchar](40) NOT NULL,
	[ContactName] [nvarchar](30) NULL,
	[ContactTitle] [nvarchar](30) NULL,
	[Address] [nvarchar](60) NULL,
	[City] [nvarchar](15) NULL,
	[Region] [nvarchar](15) NULL,
	[PostalCode] [nvarchar](10) NULL,
	[Country] [nvarchar](15) NULL,
	[Phone] [nvarchar](24) NULL,
	[Fax] [nvarchar](24) NULL,
     */
    public void testQueryWithMapResult() {
        try {
            List<LinkedHashMap<String, Object>> rs = command.query("select * from customers");
            for (Map<String, Object> row : rs) {

                log.info(row.get("CustomerID") + " " + row.get("ContactName")); // CASE SENSITIVE OR NULL! Glop idea anyway.

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }
}
