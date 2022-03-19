package net.sf.persism;

import net.sf.persism.categories.LocalDB;
import net.sf.persism.dao.Contact;
import net.sf.persism.dao.Customer;
import net.sf.persism.dao.DAOFactory;
import net.sf.persism.dao.Order;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.persism.UtilsForTests.*;

/**
 * Comments for TestDerby go here.
 *
 * @author danhoward
 * @since 12-05-22 8:26 AM
 */
@Category(LocalDB.class)
public final class TestDerby extends BaseTest {

    private static final Log log = Log.getLogger(TestDerby.class);


    @Override
    public void setUp() throws Exception {
        connectionType = ConnectionTypes.Derby;
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/derby.properties"));
        // SEE POM AND DOCUMENT THAT WE USE A NEWER VERSION.
        //Class.forName(props.getProperty("database.driver")).newInstance(); // derby needs new instance.... NO IT DOESNT ANYMORE

        String home = createHomeFolder("pinfderby");
        String url = replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url);

        createTables();

        session = new Session(con, "jdbc:derby/TESTING");

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void testContactTable() throws SQLException {
        COLUMN_FIRST_NAME = "FIRST_NAME";
        COLUMN_LAST_NAME = "LAST_NAME";

        super.testContactTable();
        assertTrue(true);
    }

    @Override
    protected void createTables() throws SQLException {
        List<String> commands = new ArrayList<String>(12);
        String sql;
        if (isTableInDatabase("Orders", con)) {
            sql = "DROP TABLE Orders";
            commands.add(sql);
        }

        sql = "CREATE TABLE Orders ( " +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                " NAME VARCHAR(30), " +
                " PAID BOOLEAN, " +
                " Prepaid BOOLEAN," +
                " IsCollect BOOLEAN," +
                " IsCancelled BOOLEAN," +
                " Customer_ID VARCHAR(10), " +
                " Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                " Date_Paid TIMESTAMP, " +
                " DateSomething TIMESTAMP" +
                ") ";

        commands.add(sql);

        if (isViewInDatabase("CustomerInvoice", con)) {
            commands.add("DROP VIEW CustomerInvoice");
        }
        if (isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " GROUP_ID INT, " +
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
                " STATUS CHAR(1), " +
                " Date_Registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                " Date_Of_Last_Order DATE, " +
                " TestLocalDate TIMESTAMP, " +
                " TestLocalDateTime TIMESTAMP " +
                ") ");

        if (isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                //" Invoice_ID INT IDENTITY PRIMARY KEY, " +
                "Invoice_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                " Customer_ID varchar(10) NOT NULL, " +
                " Paid BOOLEAN NOT NULL, " +
                " Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + // make read-only in Invoice Object
                " Status CHAR(1) DEFAULT '1', " +
                " Price NUMERIC(7,3) NOT NULL, " +
                " ACTUALPRICE NUMERIC(7,3) NOT NULL, " +
                " Quantity INT NOT NULL, " +
//                " Total NUMERIC(10,3) NOT NULL, " +
                " Discount NUMERIC(10,3) NOT NULL " +
                ") ");


        sql = "CREATE VIEW CustomerInvoice AS\n" +
                " SELECT c.Customer_ID, c.Company_Name, i.Invoice_ID, i.Status, i.Created AS DateCreated, i.PAID, i.Quantity\n" +
                "       FROM Invoices i\n" +
                "       JOIN Customers c ON i.Customer_ID = c.Customer_ID\n";
//                "       WHERE i.Status = 1\n";

        commands.add(sql);
        if (isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            commands.add("DROP TABLE TABLEMULTIPRIMARY");
        }

        commands.add("CREATE TABLE TABLEMULTIPRIMARY ( " +
                " CUSTOMER_NAME VARCHAR(30) NOT NULL, " +
                " Field4 VARCHAR(30), " +
                " Field5 TIMESTAMP, " +
                " ID INT NOT NULL " +
                ") ");

        commands.add("ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (ID, CUSTOMER_NAME)");

        executeCommands(commands, con);

        if (isTableInDatabase("Contacts", con)) {
            executeCommand("DROP TABLE Contacts", con);
        }

        sql = "CREATE TABLE Contacts(  " +
                "   \"identity\" CHAR(16) FOR BIT DATA NOT NULL PRIMARY KEY,  " +  // test binary(16)
                "   PartnerID varchar(36) NOT NULL,  " + // test varchar(36)
                "   Type char(2) NOT NULL,  " +
                "   FIRST_NAME varchar(50) NOT NULL,  " +
                "   LAST_NAME varchar(50) NOT NULL,  " +
                "   ContactName varchar(50) NOT NULL,  " +
                "   Company varchar(50) NOT NULL,  " +
                "   Division varchar(50),  " +
                "   Email varchar(50),  " +
                "   Address1 varchar(50),  " +
                "   Address2 varchar(50),  " +
                "   City varchar(50),  " +
                "   Status SMALLINT, " +
                "   StateProvince varchar(50),  " +
                "   ZipPostalCode varchar(10),  " +
                "   Country varchar(50),  " +
                "   DateAdded Date,  " +
                "   LastModified Timestamp,  " +
                "   Notes Clob,  " +
                "   AmountOwed REAL,  " +
                "   \"BigInt\" DECIMAL(20)  ,  " +
                "   SomeDate Timestamp, " +
                "   TestINstant Timestamp, " +
                "   TestINstant2 Timestamp, " +
                "   WhatMiteIsIt TIME, " +
                "   WhatTimeIsIt TIME) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTestLocalTypes", con)) {
            executeCommand("DROP TABLE DateTestLocalTypes", con);
        }

        // TIMESTAMP for DATETIME in DERBY
        sql = "CREATE TABLE DateTestLocalTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIME," +
                " DateAndTime TIMESTAMP) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTestSQLTypes", con)) {
            executeCommand("DROP TABLE DateTestSQLTypes", con);
        }

        sql = "CREATE TABLE DateTestSQLTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIME," +
                " UtilDateAndTime TIMESTAMP," +
                " DateAndTime TIMESTAMP) ";

        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("RecordTest1", con)) {
            executeCommand("DROP TABLE RecordTest1", con);
        }
        sql = "CREATE TABLE RecordTest1 ( " +
                "ID CHAR(16) FOR BIT DATA NOT NULL, " + // PRIMARY KEY
                "NAME VARCHAR(20), " +
                "QTY INT, " +
                "PRICE REAL " +
                ") ";
        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("RecordTest2", con)) {
            executeCommand("DROP TABLE RecordTest2", con);
        }
        sql = "CREATE TABLE RecordTest2 ( " +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "DESCRIPTION VARCHAR(20), " +
                "QTY INT, " +
                "PRICE REAL, " +
                "CREATED_ON TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ";
        executeCommand(sql, con);

        if (isTableInDatabase("InvoiceLineItems", con)) {
            executeCommand("DROP TABLE InvoiceLineItems", con);
        }
        sql = """
                CREATE TABLE InvoiceLineItems (
                    ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
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
                    COST NUMERIC(10,3)
                    )
                """;
        executeCommand(sql, con);


    }

    public void testTypes() {
        Statement st = null;
        ResultSet rs = null;
        try {

            st = con.createStatement();

            DatabaseMetaData dbmd = con.getMetaData();
            log.info(dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion());

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

            Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Customer.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            Order order = DAOFactory.newOrder(con);
            ;
            order.setCustomerId("123");
            order.setName("name");
            order.setCreated(LocalDate.ofEpochDay(378));
            order.setDatePaid(LocalDateTime.now());
            order.setPaid(true);

            session.insert(order);
            log.info("BEFORE " + order.getDatePaid());
            session.fetch(order);
            log.info("AFTER " + order.getDatePaid());

            log.info("ORDER:" + order);
            Order order2 = DAOFactory.newOrder(con);
            order2.setCustomerId("123");
            order2.setName("name");
            order2.setPaid(true);
            session.insert(order2);
            session.fetch(order2);

            assertNotNull("date should be defaulted?", order2.getCreated());


            columns = session.getMetaData().getColumns(Order.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            // look at meta data columsn
            columns = session.getMetaData().getColumns(Order.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            List<Order> orders = session.query(Order.class, "SELECT * FROM Orders WHERE Customer_ID = ? ORDER BY DATE_PAID", "123").
                    stream().sorted(Comparator.comparing(Order::getCreated)).collect(Collectors.toList());
            log.warn(orders);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());

        } finally {
            Util.cleanup(st, rs);
        }
    }

    @Override
    public void testAllDates() {
        super.testAllDates();

        session.query(Contact.class, "SELECT * FROM CONTACTS WHERE Last_Name = ?", "fred");
    }
}
