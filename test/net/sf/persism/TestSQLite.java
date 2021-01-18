package net.sf.persism;

/**
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/22/11
 * Time: 11:08 AM 
 */

import net.sf.persism.dao.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestSQLite extends BaseTest {

    // data types
    // http://sqlite.org/datatype3.html

    private static final Log log = Log.getLogger(TestSQLite.class);


    String home;

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/sqlite.properties"));
        Class.forName(props.getProperty("database.driver"));

        home = UtilsForTests.createHomeFolder("pinfsqlite");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url);

        con = new net.sf.log4jdbc.ConnectionSpy(con);


        createTables();

        session = new Session(con);
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOrders() {


        try {
            Order order = DAOFactory.newOrder(con);
            order.setName("COW");
            order.setPaid(true);
            session.insert(order);
            assertTrue("order id > 0", order.getId() > 0);

            assertTrue("paid", order.isPaid());

            order = DAOFactory.newOrder(con);
            order.setName("MOOO");
            order.setPaid(false);
            session.insert(order);

            order = DAOFactory.newOrder(con);
            order.setName("MEOW");
            session.insert(order);

            order = DAOFactory.newOrder(con);
            order.setName("PHHHH");
            session.insert(order);

            List<Order> list = session.query(Order.class, "SELECT * FROM Orders ORDER BY ID");
            assertEquals("list size s/b 4", 4, list.size());

            order = list.get(0);
            assertEquals("name s/b COW", "COW", order.getName());
            assertTrue("paid s/b true", order.isPaid());


            order = list.get(1);
            assertEquals("name s/b MOOO", "MOOO", order.getName());
            assertFalse("paid s/b false", order.isPaid());

            order = list.get(2);
            assertEquals("name s/b MEOW", "MEOW", order.getName());
            assertNull("paid s/b NULL", order.isPaid());

            order = list.get(3);
            assertEquals("name s/b PHHHH", "PHHHH", order.getName());

            Object x = new Date(System.currentTimeMillis());
            log.info(x.getClass());

//        java.util.Date d = new java.sql.Date(System.currentTimeMillis());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }



    public void testCustomers() {

        Customer customer = new Customer();
        customer.setCompanyName("MOO");

        // fail? It should?
        boolean nullKeyFail = false;
        try {
            session.insert(customer);
        } catch (PersismException e) {
            nullKeyFail = true;
            assertEquals("Should have constraint exception here", "[SQLITE_CONSTRAINT_NOTNULL]  A NOT NULL constraint failed (NOT NULL constraint failed: Customers.Customer_ID)", e.getMessage());
        }
        assertTrue("null key should have failed", nullKeyFail);

        customer.setCustomerId("MOO");
        session.insert(customer); // this should be ok now.

        // insert a duplicate
        boolean dupFail = false;

        Customer customer2 = new Customer();
        customer2.setCompanyName("COW");
        customer2.setCustomerId("MOO");
        try {
            session.insert(customer2);
        } catch (PersismException e) {
            dupFail = true;
            assertEquals("Should have constraint exception here", "[SQLITE_CONSTRAINT_PRIMARYKEY]  A PRIMARY KEY constraint failed (UNIQUE constraint failed: Customers.Customer_ID)", e.getMessage());
        }

        assertTrue("duplicate key should fail", dupFail);

        List<Customer> list = session.query(Customer.class, "select * from customers");

        assertEquals("list should have 1 customer", 1, list.size());

        log.info(list);

        boolean notInitialized = false;
        try {
            list = session.query(Customer.class, "select Customer_ID from Customers");
        } catch (PersismException e) {
            log.warn(e.getMessage());
            assertTrue("exception should be Customer was not properly initialized", e.getMessage().startsWith("Object class net.sf.persism.dao.Customer was not properly initialized."));
            notInitialized = true;
        }
        assertTrue("should not have initialized customer objects because the query missed some properties", notInitialized);


        customer.setCustomerId("MOO");
        customer.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));

        log.info(customer.getDateRegistered());
        session.update(customer);

        assertTrue("customer found and read", session.fetch(customer));

        log.info(customer.getDateRegistered());

        session.delete(customer);

        list = session.query(Customer.class, "select * from customers");

        assertEquals("list should have 0 customers", 0, list.size());

    }

    public void testDefaultDate() {
        Customer customer = new Customer();
        customer.setCustomerId("XYZ");
        customer.setContactName("TEST2");

        log.info(customer.getDateRegistered());
        assertNull("dated reg should be null", customer.getDateRegistered());

        session.insert(customer);


//         query.refreshObject(customer);

        log.info(customer.getDateRegistered());
        assertNotNull("date reg should be not null", customer.getDateRegistered());

    }

    public void testMultipleGeneratedKeys() {
        // test customer which has a default on Date_Registered
        // getGeneratedKeys does NOT retrieve this value.
        // getGeneratedKeys is ONLY for autoincs and guids

        String insertStatement = "INSERT INTO Customers (Customer_ID, Contact_Name) VALUES ( ?, ? ) ";

        PreparedStatement st = null;
        java.sql.ResultSet rs = null;
        try {
            String[] keyArray = {"Date_Registered"};
            st = con.prepareStatement(insertStatement, keyArray);

            st.setString(1, "JUNK");
            st.setString(1, "JUNK NAME");

            int ret = st.executeUpdate();
            log.info("rows insetred " + ret);
            rs = st.getGeneratedKeys();
            while (rs.next()) {
                log.info("cow: " + rs.getObject(1));
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            Util.cleanup(st, rs);
        }

    }

    public void testMetaData() {

        Statement st = null;
        java.sql.ResultSet rs = null;
        try {
            st = con.createStatement();
            rs = st.executeQuery("select * FROM CUSTOMERS where 1=0");
            //ResultSetMetaData rsmd = rs.getMetaData();

            DatabaseMetaData dmd = con.getMetaData();
            //rs = dmd.getColumns(null, "%", "CUSTOMERS", null);

            ResultSetMetaData rsmd = rs.getMetaData();
            rs = dmd.getVersionColumns(null, "%", "CUSTOMERS");
            int cols = rsmd.getColumnCount();
            while (rs.next()) {
                for (int j = 1; j <= cols; j++) {
                    log.info(rsmd.getColumnName(j) + " = " + rs.getObject(j));
                }
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            Util.cleanup(st, rs);
        }
    }

    // ResultSetMetaData can't determine types if there is no result? where 1=0 ?
    // http://groups.google.com/group/xerial/browse_thread/thread/2abbd5ed2ea0189?hl=en
    // TODO test copied from testH2 - need to move to common
    public void testTypes() {
        Statement st = null;
        ResultSet rs = null;

        try {

            st = con.createStatement();

            DatabaseMetaData dbmd = con.getMetaData();
            log.info(dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion());

            rs = st.executeQuery("SELECT count(*), * FROM Orders WHERE 1=0");
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
            // TODO Date and BIT come back as STRING? WTF? SQLITE?
            Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Customer.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            Order order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("name");
            order.setCreated(new java.util.Date());
            order.setPaid(true);

            session.insert(order);
            session.fetch(order);

            // look at meta data columns
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

    public void testExplain() {
        Statement st = null;
        java.sql.ResultSet rs = null;

        try {

            st = con.createStatement();

            DatabaseMetaData dbmd = con.getMetaData();
            log.info(dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion());

            rs = st.executeQuery("EXPLAIN SELECT count(*), * FROM Orders WHERE 1=0");
            log.info("FIRST? " + rs.next());
            // Grab all columns and make first pass to detect primary auto-inc
            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                log.info(rsMetaData.getColumnName(i) + " = " + rs.getObject(i));
            }
            rs.close();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());

        } finally {
            Util.cleanup(st, rs);
        }
    }

    public void testColumnAnnotation() {
        TableNoPrimary junk = new TableNoPrimary();
        junk.setId(1);
        junk.setName("JUNK");

        // This should work OK
        session.insert(junk);

        log.warn(session.query(TableNoPrimary.class, "SELECT * FROM TableNoPrimary"));

        boolean shouldFail = false;

        junk.setName("NO WORKEE!");
        try {
            session.update(junk);
        } catch (PersismException e) {
            shouldFail = true;
            assertEquals("Message s/b 'Cannot perform update. TABLENOPRIMARY has no primary keys.'",
                    "Cannot perform update. TABLENOPRIMARY has no primary keys.",
                    e.getMessage());
        }
        assertTrue(shouldFail);
    }

    public void testColumnDefaults() {

        java.sql.ResultSet rs = null;
        Statement st = null;

        try {
            st = con.createStatement();
            if (UtilsForTests.isTableInDatabase("t", con)) {
                st.execute("drop table t");
            }
            st.execute("create table t (a datetime default CURRENT_TIMESTAMP, b text)");
            st.execute("insert into t(b) values ('hello')");

            DatabaseMetaData dmd = con.getMetaData();
            rs = dmd.getColumns(null, null, "t", null);
            while (rs.next()) {
                log.info("col: " + rs.getString("COLUMN_NAME") + " DEF: " + rs.getString("COLUMN_DEF"));
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            Util.cleanup(st, rs);
        }

    }


    @Override
    protected final void createTables() throws SQLException {

        Statement st = null;
        List<String> commands = new ArrayList<String>(3);
        if (UtilsForTests.isTableInDatabase("Orders", con)) {

            //st.execute("TRUNCATE TABLE Orders");  next to lines are the equivalent
            //commands.add("DELETE FROM Orders");
            //commands.add("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'Orders'");
            commands.add("DROP TABLE Orders");

        }
        commands.add("CREATE TABLE Orders ( " +
                " ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                " NAME VARCHAR(30) NULL, " +
                " ROW_ID VARCHAR(30) NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " PAID BIT NULL, " +
                " CREATED datetime " +
                ") ");


        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY UNIQUE NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NULL, " +
                " Phone VARCHAR(30) NULL, " +
                " STATUS CHAR(1) NULL, " +
                " Fax VARCHAR(30) NULL, " +
               // " Date_Registered datetime default CURRENT_TIMESTAMP, " +
                " Date_Registered datetime default  (datetime('now','localtime')), " +
                " Date_Of_Last_Order datetime " +
                ") ");

        if (UtilsForTests.isTableInDatabase("TABLENOPRIMARY", con)) {
            commands.add("DROP TABLE TABLENOPRIMARY");
        }

        // todo WHY TF does this not throw an exception?????
        // TODO Need to review best practices for SQLite http://www.sqlite.org/datatype3.html
        commands.add("CREATE TABLE TABLENOPRIMARY ( " +
                " ID INT, " +
                " Name VARCHAR(30), " +
                " Field4 VARCHAR(30), " +
                " Field5 DATETIME, " +
                " Field6 SHITE, " +
                " Field7 FACK, " +
                " Field8 COWABUNGA " +
                ") ");

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