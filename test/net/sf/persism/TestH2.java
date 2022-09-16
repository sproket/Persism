package net.sf.persism;

import net.sf.persism.categories.LocalDB;
import net.sf.persism.dao.*;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;
import static net.sf.persism.UtilsForTests.isTableInDatabase;
import static net.sf.persism.UtilsForTests.isViewInDatabase;

/**
 * Comments for TestH2 go here.
 *
 * @author Dan Howard
 * @since 9/25/11 8:04 AM
 */
@Category(LocalDB.class)
public final class TestH2 extends BaseTest {

    // data types
    // http://www.h2database.com/html/datatypes.html

    // todo  case sensitivity tests
    // http://www.h2database.com/html/features.html
    /*
Compatibility
All database engines behave a little bit different. Where possible, H2 supports the ANSI SQL standard,
and tries to be compatible to other databases. There are still a few differences however:

In MySQL text columns are case insensitive by default, while in H2 they are case sensitive. However H2 supports
case insensitive columns as well. To create the tables with case insensitive texts, append IGNORECASE=TRUE
to the database URL (example: jdbc:h2:~/test;IGNORECASE=TRUE).
     */

    private static final Log log = Log.getLogger(TestH2.class);

    @Override
    protected void setUp() throws Exception {
        connectionType = ConnectionType.H2;
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/h2.properties"));
        Class.forName(props.getProperty("database.driver"));

        String home = UtilsForTests.createHomeFolder("pinfh2");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url, "sa", "");

        createTables();

        session = new Session(con, "jdbc:h2/H2!");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void createTables() throws SQLException {
        List<String> commands = new ArrayList<>(12);
        String sql;
        if (isTableInDatabase("Orders", con)) {
            sql = "DROP TABLE Orders";
            commands.add(sql);
        }

        sql = "CREATE TABLE Orders ( " +
              " ID IDENTITY PRIMARY KEY, " +
              " NAME VARCHAR(30) NULL, " +
              " PAID BIT NULL, " +
              " Prepaid BIT NULL," + // would match to getter? Not if it's GETPrePaid FFS
              " IsCollect BIT NULL," +
              " IsCancelled BIT NULL," + // property is IsCancelled
              " Customer_ID VARCHAR(10) NULL, " +
              " Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
              " Date_Paid TIMESTAMP NULL, " +
              " Date_Something TIMESTAMP NULL " +
              ") ";

        commands.add(sql);

        // view first
        if (isViewInDatabase("CustomerInvoice", con)) {
            commands.add("DROP VIEW CustomerInvoice");
        }

        if (isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                     " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                     " GROUP_ID INT NULL, " +
                     " Company_Name VARCHAR(30) NULL, " +
                     " Contact_Name VARCHAR(30) NULL, " +
                     " Contact_Title VARCHAR(10) NULL, " +
                     " Address VARCHAR(40) NULL, " +
                     " City VARCHAR(30) NULL, " +
                     " Region ENUM('North', 'South', 'East', 'West'), " +
                     " Postal_Code VARCHAR(10) NULL, " +
                     " Country VARCHAR(2) DEFAULT 'US', " +
                     " Phone VARCHAR(30) NULL, " +
                     " Fax VARCHAR(30) NULL, " +
                     " Status CHAR(1) NULL, " +
                     " Date_Registered datetime default current_timestamp, " +
                     " Date_Of_Last_Order DATE NULL, " +
                     " TestLocalDate date NULL, " +
                     " TestLocalDateTime datetime NULL" +
                     ") ");

        if (isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                     " Invoice_ID IDENTITY PRIMARY KEY, " +
                     " Customer_ID varchar(10) NOT NULL, " +
                     " Paid BIT NOT NULL, " +
                     " Price NUMERIC(7,3) NOT NULL, " +
                     " ActualPrice NUMERIC(7,3) NOT NULL, " +
                     " Status CHAR(1) DEFAULT '1', " +
                     " Created DateTime default current_timestamp, " + // make read-only in Invoice Object
                     " Quantity NUMERIC(10) NOT NULL, " +
                     " Discount NUMERIC(10,3) NOT NULL " +
                     ") ");


        sql = """
                CREATE VIEW CustomerInvoice AS
                    SELECT c.Customer_ID, c.Company_Name, i.Invoice_ID, i.Status, i.Created AS DateCreated, i.PAID, i.Quantity
                    FROM Invoices i
                    JOIN Customers c ON i.Customer_ID = c.Customer_ID
                """;
//                     WHERE i.Status = 1 changed to char fails with Data conversion error converting
        commands.add(sql);


        if (isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            commands.add("DROP TABLE TABLEMULTIPRIMARY");
        }

        commands.add("CREATE TABLE TABLEMULTIPRIMARY ( " +
                     " OrderID INT NOT NULL, " +
                     " ProductID VARCHAR(10) NOT NULL, " +
                     " UnitPrice DECIMAL NOT NULL, " +
                     " Quantity SMALLINT NOT NULL, " +
                     " Discount REAL NOT NULL " +
                     ") ");

        commands.add("ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (ProductID, OrderID)");


        if (isTableInDatabase("SavedGames", con)) {
            commands.add("DROP TABLE SavedGames");
        }

        commands.add("CREATE TABLE SavedGames ( " +
                     " ID VARCHAR(20) IDENTITY PRIMARY KEY, " +
                     " Name VARCHAR(100), " +
                     " Some_Date_And_Time TIMESTAMP NULL, " +
                     " Platinum REAL NULL, " +
                     " Gold REAL NULL, " +
                     " Silver REAL NULL, " +
                     " Copper REAL NULL, " +
                     " Data TEXT NULL, " +
                     " WhatTimeIsIt Time NULL, " +
                     " SomethingBig BLOB NULL) ");

        executeCommands(commands, con);

        if (isTableInDatabase("Contacts", con)) {
            executeCommand("DROP TABLE Contacts", con);
        }

        sql = "CREATE TABLE Contacts( " +
              "   identity binary(16) NOT NULL PRIMARY KEY, " +  // test binary(16)
              "   PartnerID varchar(36) NOT NULL, " + // test varchar(36)
              "   Type char(2) NOT NULL, " +
              "   Firstname varchar(50) NOT NULL, " +
              "   Lastname varchar(50) NOT NULL, " +
              "   ContactName varchar(50) NOT NULL, " +
              "   Company varchar(50) NOT NULL, " +
              "   Division varchar(50) NULL, " +
              "   Email varchar(50) NULL, " +
              "   Address1 varchar(50) NULL, " +
              "   Address2 varchar(50) NULL, " +
              "   City varchar(50) NULL, " +
              "   Status TINYINT NULL, " +
              "   StateProvince varchar(50) NULL, " +
              "   ZipPostalCode varchar(10) NULL, " +
              "   Country varchar(50) NULL, " +
              "   DateAdded Date NULL, " +
              "   LastModified DateTime NULL, " +
              "   Notes text NULL, " +
              "   AmountOwed REAL NULL, " +
              "   BigInt DECIMAL(20) NULL, " +
              "   Some_DATE Datetime NULL, " +
              "   TestInstant Datetime NULL, " +
              "   TestInstant2 DATE NULL, " + // DATE NOT SUPPORTED MAPPED TO INSTANCE UnsupportedOperationException
              "   WhatMiteIsIt TIME NULL, " +
              "   WhatTimeIsIt TIME NULL) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTest", con)) {
            executeCommand("DROP TABLE DateTest", con);
        }

        sql = "CREATE TABLE DateTest ( " +
              " ID INT, " +
              " Description VARCHAR(100), " +
              " SqlDate1 DATETIME, " +
              " SqlDate2 DATE, " +
              " LocalDate1 DATETIME, " +
              " LocalDate2 DATE, " +
              " UtilDate1 DATETIME, " +
              " UtilDate2 DATE, " +
              " Instant1 DATETIME, " +
              " Instant2 DATE, " +
              " Timestamp1 DATETIME, " +
              " Timestamp2 DATE, " +
              " LocalDateTime1 DATETIME, " +
              " LocalDateTime2 DATE, " +
              " Time1 TIME," +
              " Time2 TIME," +
              " LocalTime1 TIME," +
              " LocalTime2 TIME) ";

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
              " UtilDateAndTime DATETIME," +
              " DateAndTime DATETIME) ";

        executeCommand(sql, con);

        if (isTableInDatabase("ByteData", con)) {
            executeCommand("DROP TABLE ByteData", con);
        }
        sql = "CREATE TABLE ByteData ( " +
              "ID VARCHAR(1), " +
              "BYTE1 INT, " +
              "BYTE2 INT ) ";
        executeCommand(sql, con);

        if (isTableInDatabase("RecordTest1", con)) {
            executeCommand("DROP TABLE RecordTest1", con);
        }
        sql = "CREATE TABLE RecordTest1 ( " +
              "ID binary(16), " +
              "NAME VARCHAR(20), " +
              "QTY INT, " +
              "PRICE REAL " +
              ") ";
        executeCommand(sql, con);

        if (isTableInDatabase("RecordTest2", con)) {
            executeCommand("DROP TABLE RecordTest2", con);
        }
        sql = "CREATE TABLE RecordTest2 ( " +
              "ID IDENTITY PRIMARY KEY, " +
              "DESCRIPTION VARCHAR(20), " +
              "QTY INT, " +
              "PRICE REAL, " +
              "CREATED_ON DATETIME default current_timestamp" +
              ") ";
        executeCommand(sql, con);

        if (isTableInDatabase("USERS", con)) {
            executeCommand("DROP TABLE USERS", con);
        }

        sql = "CREATE TABLE USERS ( " +
              "   USER_NO int IDENTITY PRIMARY KEY, " +
              "   USERCODE varchar(23) NULL, " +
              "   UserPass varchar(32) NULL, " +
              "   Name varchar(50) NULL, " +
              "   PasswordLastChg datetime NULL, " +
              "   Status varchar(1) NULL, " +
              "   LastLogin datetime NULL, " +
              "   TypeOfUser varchar(1) NULL, " +
              "   License_No varchar(20) NULL, " +
              "   BillingGroup varchar(30) NULL, " +
              "   Department int NULL, " +
              "   Phone text NULL ) ";

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


        if (isTableInDatabase("InvoiceLineItems", con)) {
            executeCommand("DROP TABLE InvoiceLineItems", con);
        }
        sql = """
                CREATE TABLE InvoiceLineItems (
                    ID int IDENTITY PRIMARY KEY,
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
                    BadNumber VARCHAR(30),
                    BadDate VARCHAR(30),
                    BadTimeStamp VARCHAR(30),
                    COST NUMERIC(10,3)
                    )
                """;
        executeCommand(sql, con);

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

        if (isTableInDatabase("TABLENOPRIMARY", con)) {
            executeCommand("DROP TABLE TABLENOPRIMARY", con);
        }

        executeCommand("CREATE TABLE TABLENOPRIMARY (  ID INT,  Name VARCHAR(30),  Field4 VARCHAR(30),  Field5 DATETIME,  Field6 INT,  Field7 INT,  Field8 INT )", con);

        if (isTableInDatabase("People", con)) {
            executeCommand("DROP TABLE People", con);
        }

        sql = """
                CREATE TABLE People (
                    ID int IDENTITY PRIMARY KEY,
                    Name VARCHAR(50),
                    FatherID int,
                    MotherID int
                    )
                """;
        executeCommand(sql, con);

    }

    public void testPeople() {
        Person fred = new Person();
        fred.setName("Fred");
        session.insert(fred);

        Person wilma = new Person();
        wilma.setName("Wilma");
        session.insert(wilma);

        Person pebbles = new Person();
        pebbles.setName("Pebbles");
        pebbles.setFatherId(fred.getId());
        pebbles.setMotherId(wilma.getId());
        session.insert(pebbles);

        System.out.println(session.query(ExtendedPerson.class));
    }

    public void testHolidays() {
        CorporateHoliday holiday = new CorporateHoliday("1", "Christmas", LocalDate.of(2021, 12, 25));
        session.insert(holiday);

        holiday = new CorporateHoliday("2", "Halloween", LocalDate.of(2021, 10, 31));
        session.insert(holiday);

        List<CorporateHoliday> holidays = session.query(CorporateHoliday.class);
        assertTrue(holidays.size() > 0);
        log.info(holidays);
    }

    public void testUser() {
        User user = new User();
        user.setName("TEST 1");
        user.setUserName("login");
        user.setTypeOfUser("X");
        session.insert(user);
        assertTrue(user.getId() > 0);
    }

    public void testDontGetMetaFirstOnQuery() throws Exception {

        var sql = """
                INSERT INTO ByteData (ID, Byte1, Byte2) Values ('X', 1, 2)
                """;
        Statement st = con.createStatement();
        st.execute(sql);

        List<ByteData> list = session.query(ByteData.class, sql("select * from ByteData"));
        assertEquals("1", 1, list.size());
    }

    @Override
    public void testContactTable() throws SQLException {
        super.testContactTable();

        List<Contact> list2 = session.query(Contact.class);
        log.info(list2);


        Contact contact = getContactForTest();
        String sql = session.getMetaData().getDefaultSelectStatement(contact.getClass(), con);

        String sql2 = """
                SELECT "IDENTITY", "PARTNERID", "TYPE", "FIRSTNAME", "LASTNAME", "CONTACTNAME", "COMPANY", "DIVISION", "EMAIL", 
                "ADDRESS1", "ADDRESS2", "CITY", "STATUS", "STATEPROVINCE", "ZIPPOSTALCODE", "COUNTRY", "DATEADDED", 
                "LASTMODIFIED", "NOTES", "AMOUNTOWED", "BIGINT", "SOME_DATE", "WHATMITEISIT", 
                "WHATTIMEISIT" FROM "PUBLIC"."CONTACTS"                
                """;
        Statement st = con.createStatement();
        st.execute(sql2);

        log.info("testContactTable SQL for : " + Contact.class + " " + sql);
        // this works
        Contact other = new Contact();
        other.setIdentity(contact.getIdentity());
        session.fetch(other);

        // UUID tests
        // TODO Fails with some DBs unless you convert yourself. the question here is how to allow a user to use the converter
        List<Contact> contacts;
//        byte[] uuid = Converter.asBytes(contact.getIdentity());
//        contacts = session.query(Contact.class, sql(sql), params((Object) uuid));
//        log.info(contacts);

        // todo it can be done this way but it can only work if its a primary key - we don't detect foreign keys.
        contacts = session.query(Contact.class, params(contact.getIdentity()));
        log.info(contacts);


    }


    public void testByteData() {
        ByteData bd = new ByteData();
        bd.setId('1');
        bd.setByte1((byte) 42);
        bd.setByte2((short) 299);

        session.insert(bd);
        log.info(bd);
        session.fetch(bd);

        bd.setByte1((byte) 876); // works?
        session.update(bd);
        log.info(bd);

        session.query(ByteData.class, sql("select * from ByteData"));

        session.query(ByteData.class);
    }

    public void testH2InsertAndReadBack() throws SQLException {


        Order order = DAOFactory.newOrder(con);
        order.setName("COW");
        //order.setCreated(LocalDate.now()); // test default

        log.info("testH2InsertAndReadBack BEFORE INSERT: " + order);

        session.insert(order);
        assertTrue("order id > 0", order.getId() > 0);
        assertNotNull("date s/b set?", order.getCreated());

        log.info("testH2InsertAndReadBack AFTER INSERT: " + order);

        List<Order> list = session.query(Order.class, sql("SELECT * FROM ORDERS"));
        log.info(list);
        assertEquals("list should be 1", 1, list.size());


        order = DAOFactory.newOrder(con);
        order.setName("MOOO");
        session.insert(order);

        order = DAOFactory.newOrder(con);
        order.setName("MEOW");
        session.insert(order);

        order = DAOFactory.newOrder(con);
        order.setName("PHHHH");
        session.insert(order);

        list = session.query(Order.class, sql("SELECT * FROM Orders ORDER BY ID"));
        assertEquals("list size s/b 4", 4, list.size());
        log.info(list);

        order = list.get(0);
        assertEquals("name s/b COW", "COW", order.getName());

        order = list.get(1);
        assertEquals("name s/b MOOO", "MOOO", order.getName());

        order = list.get(2);
        assertEquals("name s/b MEOW", "MEOW", order.getName());

        order = list.get(3);
        assertEquals("name s/b PHHHH", "PHHHH", order.getName());
        log.warn("BELFORE LIOST " + list.size());
        list = session.query(Order.class, params(1, 2));
        log.warn("AFTER " + list.size());
    }

    @Override
    public void testInvoice() {
        super.testInvoice();
    }

    public void testColumnDefaults() {
        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        //customer.setCountry("US");
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Region.East);

        log.info("testColumnDefaults before: " + customer);
        assertNull("date registered should be null", customer.getDateRegistered());
        assertNull("Country should be null", customer.getCountry());

        session.insert(customer);

        log.info("testColumnDefaults after: " + customer);
        assertNotNull("date registered should NOT be null", customer.getDateRegistered());
        assertNotNull("Country should NOT be null", customer.getCountry());
        assertEquals("Country should be US", "US", customer.getCountry());
    }

    @Override
    public void testJoinsParentFetch() throws SQLException {
        super.testJoinsParentFetch();
    }

    @Override
    public void testJoinsParentQuery() throws SQLException {
        super.testJoinsParentQuery();
    }

    // ResultSetMetaData can't determine types if there is no result? where 1=0 ?
    public void testTypes() {
        Statement st = null;
        java.sql.ResultSet rs = null;

        try {

            st = con.createStatement();

            rs = st.executeQuery("SELECT * FROM Customers where 1=0");

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

            // look at meta data columsn
            Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Customer.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            Order order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("name");
            order.setCreated(LocalDate.now());
            order.setDatePaid(LocalDateTime.now());
            order.setPaid(true);

            log.info("created " + order.getCreated());
            log.info("paid " + order.getDatePaid());

            session.insert(order);
            session.fetch(order);

            log.info("AFTER created " + order.getCreated());
            log.info("AFTER paid " + order.getDatePaid());

            Order order2 = DAOFactory.newOrder(con);
            order2.setCustomerId("456");
            order2.setName("name");
            order2.setPaid(true);

            assertNull("created is null", order2.getCreated());
            session.insert(order2);

            assertNotNull("created is not null", order2.getCreated());

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


    public void testDatabaseMetaData() {

        java.sql.ResultSet rs = null;

        DatabaseMetaData dmd;
        try {
            dmd = con.getMetaData();

            // NOTE TABLE NAME IS CASE SENSITIVE in H2
            rs = dmd.getColumns(null, null, "ORDERS", null);
            while (rs.next()) {
                Object x = rs.getObject("COLUMN_DEFAULT");
                log.info("COLUMN DEFAULT " + rs.getObject("COLUMN_NAME") + " " + x);
                if (x != null) {
                    log.info(x.getClass());
                }

                Map<String, Object> map = new HashMap<>(29);

                ResultSetMetaData rsMetaData = rs.getMetaData();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    map.put(rsMetaData.getColumnName(i), rs.getObject(i));
                }
                log.info(map);
            }


        } catch (SQLException e) {
            Util.cleanup(null, rs);
        }
    }

    public void testMultiPrimary() {
        TableMultiPrimary mp = new TableMultiPrimary();
        mp.setOrderId(1);
        mp.setProductId("3");
        mp.setUnitPrice(10.23);
        mp.setQuantity((short) 256);
        mp.setDiscount(0);
        session.insert(mp);

        log.info(mp);

        mp.setDiscount(0.25f);

        session.update(mp);

        TableMultiPrimary mp2 = new TableMultiPrimary();
        mp2.setOrderId(1);
        mp2.setProductId("2");
        mp2.setUnitPrice(9.99);
        mp2.setQuantity((short) 30);
        mp2.setDiscount(0);
        session.insert(mp2);

        log.info(mp2);

        List<TableMultiPrimary> list = session.query(TableMultiPrimary.class, sql("SELECT * FROM TableMultiPrimary WHERE OrderID IN (1,2,3) AND ProductID IN (1,2,3)"));
        log.info(list);

        list = session.query(TableMultiPrimary.class, params(1, 1));
        log.info(list);

        list = session.query(TableMultiPrimary.class, params(1, 2));
        log.info(list);

        list = session.query(TableMultiPrimary.class, params(1, 2, 1, 1));
        log.info(list);

        session.fetch(mp);
        session.delete(mp);

        boolean nullInsertFail = false;
        try {
            mp = new TableMultiPrimary();
            mp.setOrderId(-1);
            mp.setProductId("x");
            session.insert(mp);
            session.insert(mp);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // Fail by double insert same primary keys
            assertTrue("message starts with 'Unique index or primary key violation'",
                    e.getMessage().startsWith("Unique index or primary key violation"));
            nullInsertFail = true;
        }

        assertTrue("nullInsertFail s/b true", nullInsertFail);

        //session.query(TableMultiPrimary.class, PrimaryKey.keys(1,1));
    }

    public void testColumnDef() {

        if (true) {
            return;
        }
        java.sql.ResultSet rs = null;
        Statement st = null;

        try {
            st = con.createStatement();
            if (isTableInDatabase("TEST_COLS", con)) {
                st.execute("drop table TEST_COLS");
            }
            st.execute("create table TEST_COLS (a datetime default current_timestamp, b text)");
            st.execute("insert into TEST_COLS (b) values ('hello')");

            DatabaseMetaData dmd = con.getMetaData();
            rs = dmd.getColumns(null, null, "TEST_COLS", null);
            log.info("COLUMNS?");
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
    public void testAllDates() {
        super.testAllDates();
    }


}
