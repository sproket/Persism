/**
 * Comments for TestPostgreSQL go here.
 * @author Dan Howard
 * @since 6/21/12 6:05 AM
 */
package net.sf.persism;

import net.sf.persism.dao.Customer;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestPostgreSQL extends BaseTest {

    private static final Log log = Log.getLogger(TestPostgreSQL.class);

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/postgresql.properties"));
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
        String sql;
        if (UtilsForTests.isTableInDatabase("Orders", con)) {
            sql = "DROP TABLE Orders";
            commands.add(sql);
        }

        sql = "CREATE TABLE Orders ( " +
                " ID SERIAL PRIMARY KEY, " +
                " NAME VARCHAR(30) NULL, " +
                " PAID BOOLEAN NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL " +
                //" Created DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL " +
                //" Created DATETIME " + // working one
                ") ";

        commands.add(sql);

        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
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
                " STATUS CHAR(1), " +
                " Date_Registered TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                " Date_Of_Last_Order  TIMESTAMP with time zone " +
                ") ");

        if (UtilsForTests.isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                " Invoice_ID SERIAL PRIMARY KEY, " +
                " Customer_ID varchar(10) NOT NULL, " +
                " Paid BOOLEAN NOT NULL, " +
                " Price NUMERIC(7,3) NOT NULL, " +
                " Quantity INT NOT NULL, " +
                " Total NUMERIC(10,3) NOT NULL " +
                ") ");


        if (UtilsForTests.isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            commands.add("DROP TABLE TABLEMULTIPRIMARY");
        }

        commands.add("CREATE TABLE TABLEMULTIPRIMARY ( " +
                " CUSTOMER_NAME VARCHAR(30) NOT NULL, " +
                " Field4 VARCHAR(30), " +
                " Field5  TIMESTAMP with time zone, " +
                " ID INT NOT NULL " +
                ") ");

        commands.add("ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (ID, CUSTOMER_NAME)");

        Statement st = null;
        try {
            st = con.createStatement();
            for (String command : commands) {
                log.info(command);
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

    public void testDefaultDate() {
        try {
            Customer customer = new Customer();
            customer.setCustomerId("MOO");
            customer.setContactName("FRED");
            customer.setStatus('1');
            session.insert(customer);
            log.info(customer);

            assertNotNull(customer.getDateRegistered());


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }
}
