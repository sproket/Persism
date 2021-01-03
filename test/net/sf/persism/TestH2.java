/**
 * Comments for TestH2 go here.
 * @author Dan Howard
 * @since 9/25/11 8:04 AM
 */
package net.sf.persism;

import net.sf.persism.dao.*;

import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;

public class TestH2 extends BaseTest {

    // data types
    // http://www.h2database.com/html/datatypes.html

    private static final Log log = Log.getLogger(TestH2.class);

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/h2.properties"));
        Class.forName(props.getProperty("database.driver"));

        String home = UtilsForTests.createHomeFolder("pinfh2");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url, "sa", "");

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        createTables();

        query = new Query(con);
        command = new Command(con);
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testH2InsertAndReadBack() {

        try {


            Order order = DAOFactory.newOrder(con);
            order.setName("COW");
            order.setCreated(new java.util.Date(System.currentTimeMillis()));

            log.info("testH2InsertAndReadBack BEFORE INSERT: " + order);

            command.insert(order);
            assertTrue("order id > 0", order.getId() > 0);

            log.info("testH2InsertAndReadBack AFTER INSERT: " + order);

            List<Order> list = query.readList(Order.class, "SELECT * FROM ORDERS");
            log.info(list);
            assertEquals("list should be 1", 1, list.size());


            order = DAOFactory.newOrder(con);
            order.setName("MOOO");
            command.insert(order);

            order = DAOFactory.newOrder(con);
            order.setName("MEOW");
            command.insert(order);

            order = DAOFactory.newOrder(con);
            order.setName("PHHHH");
            command.insert(order);

            list = query.readList(Order.class, "SELECT * FROM Orders ORDER BY ID");
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


//            CachedRowSet cachedRowSet = new CachedRowSetImpl();
//            WebRowSet priceList = new WebRowSetImpl();
//            ResultSet

//            RowSetProvider.newFactory()
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testInvoice() {

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        customer.setCountry("US");
        customer.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Regions.East);

        command.insert(customer);

        Invoice invoice = new Invoice();
        invoice.setCustomerId("MOO");
        invoice.setPrice(10.5f);
        invoice.setQuantity(10);
        invoice.setTotal(new BigDecimal(invoice.getPrice() * invoice.getQuantity()));
        invoice.setPaid(true);

        command.insert(invoice);

        assertTrue("Invoice ID > 0", invoice.getInvoiceId() > 0);

        List<Invoice> invoices = query.readList(Invoice.class, "select * from invoices where customer_id=?", "MOO");
        assertEquals("invoices s/b 1", 1, invoices.size());

        invoice = invoices.get(0);

        log.info(invoice);

        assertEquals("customer s/b MOO", "MOO", invoice.getCustomerId());
        assertEquals("invoice # s/b 1", 1, invoice.getInvoiceId());
        assertEquals("price s/b 10.5", 10.5f, invoice.getPrice());
        assertEquals("qty s/b 10", 10, invoice.getQuantity());

        NumberFormat nf = NumberFormat.getInstance();

        assertEquals("totals/b 105.00", nf.format(105.0f), nf.format(invoice.getTotal()));

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

        command.insert(customer);

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
            command.insert(customer);


            query.read(customer);

            // look at meta data columsn
            Map<String, ColumnInfo> columns = query.metaData.getColumns(Customer.class, con);
            for (ColumnInfo columnInfo : columns.values()) {
                log.info(columnInfo);
                assertNotNull("type should not be null", columnInfo.columnType);
            }

            Order order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("name");
            order.setCreated(new java.util.Date());
            order.setPaid(true);

            command.insert(order);
            query.read(order);

            // look at meta data columsn
            columns = query.metaData.getColumns(Order.class, con);
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
//            rs = dmd.getColumns(null, null, "Orders", null);
            rs = dmd.getColumns(null, null, "ORDERS", null); // TODO TABLE NAME IS CASE SENSITIVE!

            while (rs.next()) {


                Object x = rs.getObject("COLUMN_DEFAULT");
                log.info(rs.getObject("COLUMN_NAME") + " " + x);
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

    // todo need to add tests with multiple fields generated maybe in MSSQL
    // todo need to add a table where this is defined with annotations only
    public void testMultiPrimary() {
        TableMultiPrimary tmp = new TableMultiPrimary();
        tmp.setId(1);
        tmp.setCustomerName("one");
        tmp.setField4("field 4");
        tmp.setField5(new java.util.Date());
        command.insert(tmp);

        tmp.setField4("field 5");

        command.update(tmp);

        boolean nullInsertFail = false;
        try {
            tmp = new TableMultiPrimary();

            command.insert(tmp);

        } catch (Exception e) {
            assertTrue("contains NULL not allowed for column \"CUSTOMER_NAME\"", e.getMessage().contains("NULL not allowed for column \"CUSTOMER_NAME\""));
            nullInsertFail = true;
        }

        assertTrue("nullInsertFail s/b true", nullInsertFail);

        tmp.setId(1);
        tmp.setCustomerName("one");
        assertNull("field 4 s/b null", tmp.getField4());
        assertNull("field 5 s/b null", tmp.getField5());

        log.info("BEFORE: " + tmp);

        query.read(tmp);
        assertNotNull("field 4 s/b null", tmp.getField4());
        assertNotNull("field 5 s/b null", tmp.getField5());

        log.info("AFTER: " + tmp);

    }

    public void testColumnDef() {

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
                " ID IDENTITY PRIMARY KEY, " +
                " NAME VARCHAR(30) NULL, " +
                " PAID BIT NULL, " +
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
                " Date_Registered datetime default current_timestamp, " +
                " Date_Of_Last_Order datetime " +
                ") ");

        if (UtilsForTests.isTableInDatabase("Invoices", con)) {
            commands.add("DROP TABLE Invoices");
        }

        commands.add("CREATE TABLE Invoices ( " +
                " Invoice_ID IDENTITY PRIMARY KEY, " +
                " Customer_ID varchar(10) NOT NULL, " +
                " Paid BIT NOT NULL, " +
                " Price NUMERIC(7,3) NOT NULL, " +
                " Quantity INT NOT NULL, " +
                " Total NUMERIC(10,3) NOT NULL " +
                ") ");


        if (UtilsForTests.isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            commands.add("DROP TABLE TABLEMULTIPRIMARY");
        }

        if (UtilsForTests.isTableInDatabase("SavedGames", con)) {
            commands.add("DROP TABLE SavedGames");
        }

        commands.add("CREATE TABLE TABLEMULTIPRIMARY ( " +
                " CUSTOMER_NAME VARCHAR(30) NOT NULL, " +
                " Field4 VARCHAR(30), " +
                " Field5 DATETIME, " +
                " ID INT NOT NULL " +
                ") ");

        commands.add("ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (ID, CUSTOMER_NAME)");


        commands.add("CREATE TABLE SavedGames ( " +
                " ID INT IDENTITY PRIMARY KEY, " +
                " Name VARCHAR(100), " +
                " Timestamp TIMESTAMP NULL, " +
                " Data TEXT NULL ) ");

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

    public void testClob() throws SQLException {

        SavedGame sg = new SavedGame();
        sg.setName("BLAH");
        sg.setTimeStamp(new Date());
        sg.setData("HJ LHLH H H                     ';lk ;lk ';l k                                K HLHLHH LH LH LH LHLHLHH LH H H H LH HHLGHLJHGHGFHGFGJFDGHFDHFDGJFDKGHDGJFDD KHGD KHG DKHDTG HKG DFGHK  GLJHG LJHG LJH GLJ");

        command.insert(sg);

        sg = null;
        sg = query.read(SavedGame.class, "select * from SavedGames");
        log.info(sg.getData());

    }

    private void createSavedGames() {


        String sql = "CREATE TABLE SavedGames ( " +
                " ID INT IDENTITY PRIMARY KEY, " +
                " Name VARCHAR(100), " +
                " Timestamp TIMESTAMP NULL, " +
                " Data TEXT NULL ) ";

        Statement st = null;
        try {
            st = con.createStatement();
            st.execute(sql);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
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
