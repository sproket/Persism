package net.sf.persism;

import net.sf.persism.dao.Customer;
import net.sf.persism.dao.DAOFactory;
import net.sf.persism.dao.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Comments for TestDerby go here.
 *
 * @author danhoward
 * @since 12-05-22 8:26 AM
 */
public class TestDerby extends BaseTest {

    private static final Log log = Log.getLogger(TestDerby.class);


    String home;


    @Override
    public void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/derby.properties"));
        Class.forName(props.getProperty("database.driver")).newInstance(); // derby needs new instance....

        home = UtilsForTests.createHomeFolder("pinfderby");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url);

        con = new net.sf.log4jdbc.ConnectionSpy(con);


        createTables();

        session = new Session(con);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testName() throws Exception {
        log.info("cow");
    }


    // TODO test copied from testH2 - need to move to common
    public void testTypes() {
        Statement st = null;
        java.sql.ResultSet rs = null;

        try {

            st = con.createStatement();

            DatabaseMetaData dbmd = con.getMetaData();
            log.info(dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion());

            //rs = st.executeQuery("SELECT count(*), * FROM Orders WHERE 1=0");
            // todo query above does not work with Derby
            rs = st.executeQuery("SELECT * FROM Orders WHERE 1=0");
            log.info("FIRST? " + rs.next());
            // Grab all columns and make first pass to detect primary auto-inc
            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                log.info(rsMetaData.getColumnName(i) + " " + rsMetaData.isAutoIncrement(i) + " " + rsMetaData.getColumnType(i));
            }
            rs.close();

            Customer customer = new Customer();
            customer.setCustomerId("123");
            customer.setContactName("FRED");
            session.insert(customer);


            session.fetch(customer);

            // look at meta data columns
            // TODO Derby Date and BIT come back as STRING? WTF?
            Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Customer.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            Order order = DAOFactory.newOrder(con);;
            order.setCustomerId("123");
            order.setName("name");
            order.setCreated(new java.util.Date());
            order.setPaid(true);

            session.insert(order);
            session.fetch(order);

            // look at meta data columsn
            columns = session.getMetaData().getColumns(Order.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }


        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());

        } finally {
            Util.cleanup(st, rs);
        }
    }

    @Override
    protected void createTables() throws SQLException {
        List<String> commands = new ArrayList<String>(12);
        String sql;
        if (UtilsForTests.isTableInDatabase("Orders", con)) {
            sql = "DROP TABLE Orders";
            commands.add(sql);
        }

        // TODO this does not work. Date is not returned after insert.
        sql = "CREATE TABLE Orders ( " +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                " NAME VARCHAR(30), " +
                " PAID BOOLEAN, " +
                " Customer_ID VARCHAR(10), " +
                " Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL " +
                ") ";

        commands.add(sql);

        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30), " +
                " Contact_Name VARCHAR(30), " +
                " Contact_Title VARCHAR(10), " +
                " Address VARCHAR(40), " +
                " City VARCHAR(30), " +
                " Region VARCHAR(10), " +
                " Postal_Code VARCHAR(10), " +
                " Country VARCHAR(2) NOT NULL DEFAULT 'US', " +
                " Phone VARCHAR(30), " +
                " Fax VARCHAR(30), " +
                " Date_Registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                " Date_Of_Last_Order TIMESTAMP " +
                ") ");

        if (UtilsForTests.isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                //" Invoice_ID INT IDENTITY PRIMARY KEY, " +
                "Invoice_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
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
                " Field5 TIMESTAMP, " +
                " ID INT NOT NULL " +
                ") ");

        commands.add("ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (ID, CUSTOMER_NAME)");

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
}
