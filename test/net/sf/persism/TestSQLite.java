package net.sf.persism;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/22/11
 * Time: 11:08 AM
 */

import net.sf.persism.categories.LocalDB;
import net.sf.persism.dao.Customer;
import net.sf.persism.dao.DAOFactory;
import net.sf.persism.dao.Order;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static net.sf.persism.SQL.sql;
import static net.sf.persism.UtilsForTests.*;

@Category(LocalDB.class)
public final class TestSQLite extends BaseTest {

    // data types
    // http://sqlite.org/datatype3.html

    private static final Log log = Log.getLogger(TestSQLite.class);


    String home;

    @Override
    protected void setUp() throws Exception {
        connectionType = ConnectionTypes.SQLite;
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/sqlite.properties"));
        Class.forName(props.getProperty("database.driver"));

        log.info("SQLite DLL HERE: " + System.getProperty("java.io.tmpdir"));
        home = createHomeFolder("pinfsqlite");
        String url = replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url);

        createTables();

        session = new Session(con);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void createTables() throws SQLException {

        Statement st = null;
        List<String> commands = new ArrayList<String>(3);
        if (isTableInDatabase("Orders", con)) {

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
                     " Prepaid BIT NULL," +
                     " IsCollect BIT NULL," +
                     " IsCancelled BIT NULL," +
                     " CREATED datetime DEFAULT CURRENT_TIMESTAMP, " +
                     " DATE_PAID datetime NULL, " +
                     " DATE_SOMETHING datetime NULL" +
                     ") ");

        // view first
        if (isViewInDatabase("CustomerInvoice", con)) {
            commands.add("DROP VIEW CustomerInvoice");
        }


        if (isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                     " Customer_ID varchar(10) PRIMARY KEY UNIQUE NOT NULL, " +
                     " GROUP_ID INT NULL, " +
                     " Company_Name VARCHAR(30) NULL, " +
                     " Contact_Name VARCHAR(30) NULL, " +
                     " Contact_Title VARCHAR(10) NULL, " +
                     " Address VARCHAR(40) NULL, " +
                     " City VARCHAR(30) NULL, " +
                     " Region VARCHAR(10) NULL, " +
                     " Postal_Code VARCHAR(10) NULL, " +
                     " Country VARCHAR(2) DEFAULT 'US', " +
                     " Phone VARCHAR(30) NULL, " +
                     " STATUS CHAR(1) NULL, " +
                     " Fax VARCHAR(30) NULL, " +
                     " Date_Registered datetime default  (datetime('now','localtime')), " +
                     " Date_Of_Last_Order DATE, " +
                     " TestLocalDate datetime, " +
                     " TestLocalDateTIme datetime " +
                     ") ");


        if (isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                     " Invoice_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                     " Customer_ID varchar(10) NOT NULL, " +
                     " Paid BIT NOT NULL, " +
                     " Price REAL NOT NULL, " +
                     " ActualPrice REAL NOT NULL, " +
                     " Status CHAR(1) DEFAULT '1', " +
                     " Created DateTime default (datetime('now','localtime')), " + // make read-only in Invoice Object
                     " Quantity INTEGER NOT NULL, " +
                     //" Total REAL NOT NULL, " +
                     " Discount REAL NOT NULL " +
                     ") ");

        if (isTableInDatabase("TABLENOPRIMARY", con)) {
            commands.add("DROP TABLE TABLENOPRIMARY");
        }

        // WHY TF does this not throw an exception????? ANY TYPE?
        // Need to review best practices for SQLite http://www.sqlite.org/datatype3.html
        commands.add("CREATE TABLE TABLENOPRIMARY ( " +
                     " ID INT, " +
                     " Name VARCHAR(30), " +
                     " Field4 VARCHAR(30), " +
                     " Field5 DATETIME, " +
                     " Field6 SHITE, " +
                     " Field7 FACK, " +
                     " Field8 COWABUNGA " +
                     ") ");

        executeCommands(commands, con);

        if (isTableInDatabase("CONTACTS", con)) {
            executeCommand("DROP TABLE CONTACTS", con);
        }

        String sql = "CREATE TABLE CONTACTS ( " +
                     " identity VARCHAR(36) PRIMARY KEY UNIQUE NOT NULL, " +
                     " PartnerID BLOB NOT NULL, " +
                     " Type char(2) NOT NULL, " +
                     " Firstname varchar(50) NULL, " +
                     " Lastname varchar(50) NULL, " +
                     " ContactName varchar(50) NULL, " +
                     " Company varchar(50) NULL, " +
                     " Division varchar(50) NULL, " +
                     " Email varchar(50) NULL, " +
                     " Address1 varchar(50) NULL, " +
                     " Address2 varchar(50) NULL, " +
                     " City varchar(50) NULL, " +
                     " StateProvince varchar(50) NULL, " +
                     " ZipPostalCode varchar(10) NULL, " +
                     " Status SMALLINT NOT NULL, " +
                     " Country varchar(50) NULL, " +
                     " DateAdded TIMESTAMP NULL, " + // was DATETIME. What is TIMESTAMP in SQLite? RANDOM I guess.
                     " LastModified DATETIME NULL, " +
                     " Notes text NULL, " +
                     " AmountOwed float NULL, " +
                     " BigInt DECIMAL(20) NULL, " +
                     " Some_DATE DATETIME NULL, " +
                     " TestInstant TIMESTAMP NULL, " +
                     " TestInstant2 DATETIME NULL, " +
                     " WhatMiteIsIt time NULL, " +
                     " WhatTimeIsIt time NULL " +
                     ") ";
        executeCommand(sql, con);

        if (isTableInDatabase("DateTestLocalTypes", con)) {
            executeCommand("DROP TABLE DateTestLocalTypes", con);
        }

        sql = "CREATE TABLE DateTestLocalTypes ( " +
              " ID INT, " +
              " Description VARCHAR(100), " +
              " DateOnly DATE, " +
              " TimeOnly TIME," +
              " DateAndTime DATETIME) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTestSQLTypes", con)) {
            executeCommand("DROP TABLE DateTestSQLTypes", con);
        }

        sql = "CREATE TABLE DateTestSQLTypes ( " +
              " ID INT, " +
              " Description VARCHAR(100), " +
              " DateOnly DATE, " +
              " TimeOnly TIME," +
              " UtilDateAndTime DATETIME, " +
              " DateAndTime DATETIME) ";

        executeCommand(sql, con);


        if (isTableInDatabase("RecordTest1", con)) {
            executeCommand("DROP TABLE RecordTest1", con);
        }
        sql = "CREATE TABLE RecordTest1 ( " +
              "ID VARCHAR(36), " +
              "NAME VARCHAR(20), " +
              "QTY INT, " +
              "PRICE REAL " +
              ") ";
        executeCommand(sql, con);

        if (isTableInDatabase("RecordTest2", con)) {
            executeCommand("DROP TABLE RecordTest2", con);
        }
        sql = "CREATE TABLE RecordTest2 ( " +
              "ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
              "DESCRIPTION VARCHAR(20), " +
              "QTY INT, " +
              "PRICE REAL, " +
              "CREATED_ON DATETIME default current_timestamp" +
              ") ";
        executeCommand(sql, con);

        sql = """
                CREATE VIEW CustomerInvoice AS
                    SELECT c.Customer_ID, c.Company_Name, i.Invoice_ID, i.Status, i.Created AS DateCreated, i.PAID, i.Quantity
                    FROM Invoices i
                    JOIN Customers c ON i.Customer_ID = c.Customer_ID
                """;
//         WHERE i.Status = 1
        executeCommand(sql, con);

        if (isTableInDatabase("InvoiceLineItems", con)) {
            executeCommand("DROP TABLE InvoiceLineItems", con);
        }
        sql = """
                CREATE TABLE InvoiceLineItems (
                    ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
                    INVOICE_ID int,
                    Product_ID int,
                    Quantity int
                    )
                """;
        executeCommand(sql, con);

        if (isTableInDatabase("Products", con)) {
            executeCommand("DROP TABLE Products", con);
        }
        sql = """
                CREATE TABLE Products (
                    ID int,
                    Description VARCHAR(50),
                    BADNUMBER VARCHAR(30),
                    BADDATE VARCHAR(30),
                    BADTIMESTAMP VARCHAR(30),
                    COST NUMERIC(10,3)
                    )
                """;
        executeCommand(sql, con);

        if (isTableInDatabase("SavedGames", con)) {
            executeCommand("DROP TABLE SavedGames", con);
        }

        executeCommand("CREATE TABLE SavedGames ( " +
                       " ID VARCHAR(20) NOT NULL PRIMARY KEY, " +
                       " Name VARCHAR(100), " +
                       " Some_Date_And_Time DATETIME NULL, " +
                       " Platinum REAL NULL, " +
                       " Gold REAL NULL, " +
                       " Silver REAL NULL, " +
                       " Copper REAL NULL, " +
                       " Data CLOB NULL, " +
                       " WhatTimeIsIt DATETIME NULL, " +
                       " SomethingBig BLOB NULL) ", con);

        if (isTableInDatabase("Postman", con)) {
            executeCommand("DROP TABLE Postman", con);
        }
        sql = """
                CREATE TABLE Postman (
                    AUTO VARCHAR(50),
                    Host VARCHAR(50),
                    Port NUMERIC(8),
                    User VARCHAR(50),
                    Password VARCHAR(50),
                    missingGetter NUMERIC(10,3)
                    )
                """;
        executeCommand(sql, con);

        // Test for Y ending table who's plural isn't ies....
        if (isTableInDatabase("CorporateHolidays", con)) {
            executeCommand("DROP TABLE CorporateHolidays", con);
        }
        sql = """
                CREATE TABLE CorporateHolidays (
                    ID varchar(10),
                    NAME varchar(40),
                    DATE date
                    )
                """;
        executeCommand(sql, con);

    }

    @Override
    public void testContactTable() throws SQLException {
        super.testContactTable();
    }

    public void testOrders() throws Exception {

        Order order = DAOFactory.newOrder(con);
        order.setName("COW");
        order.setPaid(true);
        order.setDatePaid(LocalDateTime.now());
        session.insert(order);
        assertTrue("order id > 0", order.getId() > 0);
        log.info("DATE PAID: " + order.getDatePaid());

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

        List<Order> list = session.query(Order.class, sql("SELECT * FROM Orders ORDER BY ID"));
        assertEquals("list size s/b 4", 4, list.size());
        log.info("ORDERS\n" + list);

        session.query(Order.class, "select * from orders where id in (?,?,?)", 1, 2, 43);
        session.fetch(Order.class, "select * from orders where id = ?", 2);

        order = list.get(0);
        assertNotNull(order);
        assertEquals("name s/b COW", "COW", order.getName());
        assertTrue("paid s/b true", order.isPaid());


        order = list.get(1);
        assertNotNull(order);
        assertEquals("name s/b MOOO", "MOOO", order.getName());
        assertFalse("paid s/b false", order.isPaid());

        order = list.get(2);
        assertEquals("name s/b MEOW", "MEOW", order.getName());
        assertNull("paid s/b NULL", order.isPaid());

        order = list.get(3);
        assertEquals("name s/b PHHHH", "PHHHH", order.getName());

        Object x = new Date(System.currentTimeMillis());
        log.info(x.getClass());

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
        //customer.setDateOfLastOrder(new Date(System.currentTimeMillis()));
        customer.setDateOfLastOrder(LocalDateTime.now());
        customer.setDateRegistered(new Timestamp(System.currentTimeMillis() - 10000000l));
        customer.setContactTitle("testing 1.2.3");
        session.insert(customer); // this should be ok now.

        log.info("Customer 1 ?" + customer);

        List<Customer> list;
        list = session.query(Customer.class, "SELECT *, Company_Name, Contact_Name, :contactTitle FROM  CUSTOMERS");

        log.info(list);

        // somehow SQLite is OK with this. <sigh>
        String result = session.fetch(String.class, "SELECT :contactTitle FROM CUSTOMERS");
        log.warn("WTF! " + result);

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

        list = session.query(Customer.class, sql("select * from customers"));

        assertEquals("list should have 1 customer", 1, list.size());

        log.info(list);

        boolean notInitialized = false;
        try {
            list = session.query(Customer.class, sql("select Customer_ID from Customers"));
        } catch (PersismException e) {
            log.info(e.getMessage());
            assertTrue("exception should be Customer was not properly initialized", e.getMessage().startsWith("Object class net.sf.persism.dao.Customer was not properly initialized."));
            notInitialized = true;
        }
        assertTrue("should not have initialized customer objects because the query missed some properties", notInitialized);


        customer.setCustomerId("MOO");
        customer.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));

        log.info(customer.getDateRegistered());
        session.update(customer);

        assertTrue("customer found and read", session.fetch(customer));

        log.info(customer.getDateRegistered());

        session.delete(customer);

        list = session.query(Customer.class, sql("select * from customers"));

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
            //customer.setDateOfLastOrder(new Date(System.currentTimeMillis() - 1000000l));
            customer.setDateOfLastOrder(LocalDateTime.ofEpochSecond(System.currentTimeMillis() - 1000000l, 0, ZoneOffset.UTC));
            session.insert(customer);


            session.fetch(customer);

            // look at meta data columns
            Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Customer.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            Order order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("name");
            order.setCreated(LocalDate.now());
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
        ResultSet rs = null;

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


    public void testColumnDefaults() {

        java.sql.ResultSet rs = null;
        Statement st = null;

        try {
            st = con.createStatement();
            if (isTableInDatabase("t", con)) {
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
    public void testInvoice() {
        super.testInvoice();
    }

    @Override
    public void testAllDates() {
        super.testAllDates();
    }

    @Override
    public void testRecord1() {
        super.testRecord1();
    }

    @Override
    public void testRecord2() {
        super.testRecord2();
    }

    @Override
    public void testGetDbMetaData() throws SQLException {
        super.testGetDbMetaData();
    }
}