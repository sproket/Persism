/**
 * Comments for TestMySQL go here.
 * @author Dan Howard
 * @since 6/4/12 9:52 PM
 */
package net.sf.persism;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestMySQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMySQL.class);

    // TODO same as MSSQL - add a flag to use mariadb driver - see downloads folder
    protected void setUp() throws Exception {

        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/mysql.properties"));

        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");

        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        createTables();

        session = new Session(con);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void createTables() throws SQLException {

        List<String> commands = new ArrayList<String>(12);

        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }
        if (UtilsForTests.isTableInDatabase("Orders", con)) {
            commands.add("DROP TABLE Orders");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NOT NULL DEFAULT 'US', " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Date_Registered TIMESTAMP, " +
                " Date_Of_Last_Order DATETIME NULL " +
                ") ");

        commands.add("CREATE TABLE Orders ( " +
                " ID INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(ID), " +
                " NAME VARCHAR(30) NULL, " +
                " PAID BIT NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " Created TIMESTAMP " +
                ") ");


        Statement st = null;
        try {
            st = con.createStatement();
            for (String command : commands) {
                st.execute(command);

            }

        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }

    }


    public void testSomething() {
        try {
            //@todo write testcase for TestMySQL  
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }
}
