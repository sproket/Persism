package net.sf.persism;

import net.sf.persism.categories.LocalDB;
import net.sf.persism.dao.*;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.*;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;
import java.util.Date;

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

    private static final Log log = Log.getLogger(TestH2.class);

    @Override
    protected void setUp() throws Exception {
        connectionType = ConnectionTypes.H2;
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/h2.properties"));
        Class.forName(props.getProperty("database.driver"));

        String home = UtilsForTests.createHomeFolder("pinfh2");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url, "sa", "");

        createTables();

        session = new Session(con);

        Instant x = new Date().toInstant();

//        new java.sql.Date(x.toEpochMilli()).toInstant();
        // Method threw 'java.lang.UnsupportedOperationException' exception.

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void testContactTable() throws SQLException {
        super.testContactTable();
        assertTrue(true);

        Contact contact = getContactForTest();
        String sql = session.getMetaData().getSelectStatement(contact, con);

        // this works
        Contact other = new Contact();
        other.setIdentity(contact.getIdentity());
        session.fetch(other);

        // todo the question here is how to allow a user to use the converter
        // Fails with some DBs unless you convert yourself.
        List<Contact> contacts = session.query(Contact.class, sql, (Object) Convertor.asBytes(contact.getIdentity()));
        log.info(contacts);

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
                " Region ENUM('North', 'South', 'East', 'West'), " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NOT NULL DEFAULT 'US', " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Status CHAR(1) NULL, " +
                " Date_Registered datetime default current_timestamp, " +
                " Date_Of_Last_Order DATE NULL, " +
                " TestLocalDate date NULL, " +
                " TestLocalDateTime datetime NULL" +
                ") ");

        if (UtilsForTests.isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                " Invoice_ID IDENTITY PRIMARY KEY, " +
                " Customer_ID varchar(10) NOT NULL, " +
                " Paid BIT NOT NULL, " +
                " Price NUMERIC(7,3) NOT NULL, " +
                " ACTUALPRICE NUMERIC(7,3) NOT NULL, " +
                " Status INT DEFAULT 1, " +
                " Created DateTime default current_timestamp, " + // make read-only in Invoice Object
                " Quantity NUMERIC(10) NOT NULL, " +
                " Discount NUMERIC(10,3) NOT NULL " +
                ") ");


        if (UtilsForTests.isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            commands.add("DROP TABLE TABLEMULTIPRIMARY");
        }

        if (UtilsForTests.isTableInDatabase("SavedGames", con)) {
            commands.add("DROP TABLE SavedGames");
        }

        commands.add("CREATE TABLE TABLEMULTIPRIMARY ( " +
                " OrderID INT NOT NULL, " +
                " ProductID INT NOT NULL, " +
                " UnitPrice DECIMAL NOT NULL, " +
                " Quantity SMALLINT NOT NULL, " +
                " Discount REAL NOT NULL " +
                ") ");

        commands.add("ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (OrderID, ProductID)");

//?
        commands.add("CREATE TABLE SavedGames ( " +
                " ID VARCHAR(20) IDENTITY PRIMARY KEY, " +
                " Name VARCHAR(100), " +
                " Some_Date_And_Time TIMESTAMP NULL, " +
                " Gold REAL NULL, " +
                " Silver REAL NULL, " +
                " Copper REAL NULL, " +
                " Data TEXT NULL, " +
                " WhatTimeIsIt Time NULL, " +
                " SomethingBig BLOB NULL) ");

        executeCommands(commands, con);

        if (UtilsForTests.isTableInDatabase("Contacts", con)) {
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

        if (UtilsForTests.isTableInDatabase("DateTest", con)) {
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

        if (UtilsForTests.isTableInDatabase("DateTestLocalTypes", con)) {
            executeCommand("DROP TABLE DateTestLocalTypes", con);
        }

        sql = "CREATE TABLE DateTestLocalTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIME," +
                " DateAndTime DATETIME) ";

        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("DateTestSQLTypes", con)) {
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

        if (UtilsForTests.isTableInDatabase("ByteData", con)) {
            executeCommand("DROP TABLE ByteData", con);
        }
        sql = "CREATE TABLE ByteData ( " +
                "ID VARCHAR(1), " +
                "BYTE1 INT, " +
                "BYTE2 INT ) ";
        executeCommand(sql, con);
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

        session.query(ByteData.class, "select * from ByteData");
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

        List<Order> list = session.query(Order.class, "SELECT * FROM ORDERS");
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

        list = session.query(Order.class, "SELECT * FROM Orders ORDER BY ID");
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
        customer.setRegion(Regions.East);

        log.info("testColumnDefaults before: " + customer);
        assertNull("date registered should be null", customer.getDateRegistered());
        assertNull("Country should be null", customer.getCountry());

        session.insert(customer);

        log.info("testColumnDefaults after: " + customer);
        assertNotNull("date registered should NOT be null", customer.getDateRegistered());
        assertNotNull("Country should NOT be null", customer.getCountry());
        assertEquals("Country should be US", "US", customer.getCountry());
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

        Statement st = null;
        java.sql.ResultSet rs = null;

        DatabaseMetaData dmd = null;
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

                Map<String, Object> map = new HashMap<String, Object>(29);

                ResultSetMetaData rsMetaData = rs.getMetaData();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    map.put(rsMetaData.getColumnName(i), rs.getObject(i));
                }
                log.info(map);
            }


        } catch (SQLException e) {
            Util.cleanup(st, rs);
        }
    }

    public void testMultiPrimary() {
        TableMultiPrimary tmp = new TableMultiPrimary();
        tmp.setOrderId(1);
        tmp.setProductId(1);
        tmp.setUnitPrice(10.23);
        tmp.setQuantity((short) 256);
        tmp.setDiscount(0);
        session.insert(tmp);
        log.info(tmp);

        tmp.setDiscount(0.25f);

        session.update(tmp);

        List<TableMultiPrimary> list = session.query(TableMultiPrimary.class, "SELECT * FROM TableMultiPrimary");
        log.info(list);

        session.fetch(tmp);
        session.delete(tmp);

        boolean nullInsertFail = false;
        try {
            tmp = new TableMultiPrimary();
            session.insert(tmp);
            session.insert(tmp);
            session.insert(tmp);
            session.insert(tmp);
            session.insert(tmp);

        } catch (Exception e) {
            log.info(e.getMessage());
            assertTrue("message starts with 'Unique index or primary key violation'",
                    e.getMessage().startsWith("Unique index or primary key violation"));
            nullInsertFail = true;
        }

        assertTrue("nullInsertFail s/b true", nullInsertFail);
    }

    public void testColumnDef() {

        if (true) {
            return;
        }
        java.sql.ResultSet rs = null;
        Statement st = null;

        try {
            st = con.createStatement();
            if (UtilsForTests.isTableInDatabase("TEST_COLS", con)) {
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

    public void testVariousTypesLikeClobAndBlob() throws SQLException, IOException {
        // note Data is read as a CLOB
        SavedGame saveGame = new SavedGame();
        saveGame.setName("BLAH");
        saveGame.setSomeDateAndTime(new Date());
        saveGame.setData("HJ LHLH H H                     ';lk ;lk ';l k                                K HLHLHH LH LH LH LHLHLHH LH H H H LH HHLGHLJHGHGFHGFGJFDGHFDHFDGJFDKGHDGJFDD KHGD KHG DKHDTG HKG DFGHK  GLJHG LJHG LJH GLJ");
        saveGame.setGold(100.23f);
        saveGame.setSilver(200);
        saveGame.setCopper(100l);
        saveGame.setWhatTimeIsIt(new Time(System.currentTimeMillis()));

        File file = new File("c:/windows/explorer.exe");
        saveGame.setSomethingBig(Files.readAllBytes(file.toPath()));
        int size = saveGame.getSomethingBig().length;
        log.info("SIZE?" + saveGame.getSomethingBig().length);
        session.insert(saveGame);

        SavedGame returnedSavedGame = new SavedGame();
        returnedSavedGame.setId(saveGame.getId());
        assertTrue(session.fetch(returnedSavedGame));
        // test that a util date returned has a time still in it.
        Calendar cal = Calendar.getInstance();
        cal.setTime(returnedSavedGame.getSomeDateAndTime());
        log.info("WHAT DO THESE LOOK LIKE? " + returnedSavedGame.getSomeDateAndTime());
        log.info(" ETC>>> " + returnedSavedGame.getWhatTimeIsIt());
        assertTrue("TIME s/b > 0 - we should have time:", cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) > 0);

        saveGame = session.fetch(SavedGame.class, "select * from SavedGames");
        assertNotNull(saveGame);
        log.info("SAVED GOLD: " + saveGame.getGold());
        log.info("SAVED SILVER: " + saveGame.getSilver());
        log.info("AFTER FETCH SIZE?" + saveGame.getSomethingBig().length);
        assertEquals("size should be the same ", size, saveGame.getSomethingBig().length);

        saveGame.setSomethingBig(null);
        session.update(saveGame);
        session.fetch(saveGame);
    }


    @Override
    public void testAllDates() {
        super.testAllDates();
    }
}
