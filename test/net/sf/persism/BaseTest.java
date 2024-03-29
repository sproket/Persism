package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.*;
import net.sf.persism.dao.records.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

import static net.sf.persism.Message.NumberFormatException;
import static net.sf.persism.Message.*;
import static net.sf.persism.Parameters.none;
import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.*;
import static net.sf.persism.UtilsForTests.isTableInDatabase;

/**
 * Comments for BaseTest go here.
 *
 * @author Dan Howard
 * @since 10/8/11 6:24 PM
 */
public abstract class BaseTest extends TestCase {

    private static final Log log = Log.getLogger(BaseTest.class);

    Connection con;

    Session session;

    ConnectionType connectionType;

    static String UUID1 = "d316ad81-946d-416b-98e3-3f3b03aa73db";
    static String UUID2 = "a0d00c5a-3de6-4ae8-ba11-e3e02c2b3a83";
    static String UUID3 = "d0d00a5c-4de6-4ae8-ba33-f3e02c2b3a84";

    String COLUMN_FIRST_NAME = "FirstName";
    String COLUMN_LAST_NAME = "LastName";

    protected void createTables() throws SQLException {
        createMultiMatch("MultiMatch", connectionType);
        createMultiMatch("Multi Match", connectionType);
    }

    @Override
    protected void setUp() throws Exception {
        log.info("LOG MODE: " + log.getLogMode() + " " + log.getLogName());
        assertNotNull(connectionType);
        super.setUp();
    }


    @Override
    protected void tearDown() throws Exception {
        if (con != null) {
            //MetaData.removeInstance(con);
            con.close();
        }
        super.tearDown();
    }

    public final void messageTester(String message, Runnable block) {
        boolean fail = false;

        try {
            block.run();
        } catch (PersismException e) {
            assertEquals("s/b same", message, e.getMessage());
            fail = true;
        }
        assertTrue(fail);
    }

    public void testDates() {
        List<Customer> list = session.query(Customer.class, sql("select * from Customers"), none());
        log.info(list);

        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setAddress("123 Sesame Street");
        customer.setCity("MTL");
        customer.setCompanyName("ABC Inc");
        customer.setContactName("Fred");
        customer.setContactTitle("LORD");
        customer.setCountry("US");

        customer.setFax("fax");
        customer.setPhone("phone");
        customer.setPostalCode("12345");
        customer.setRegion(Region.East);
        customer.setStatus('2');

        String dateOfLastOrder = "20120528000000"; //sql.date - no time in it.
        String dateRegistered = "20110612185225";
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        customer.setDateOfLastOrder(LocalDateTime.parse("20120528010101", dtf)); // add time to see it removed
        log.info(customer.getDateOfLastOrder());
        customer.setDateRegistered(new Timestamp(UtilsForTests.getCalendarFromAnsiDateString(dateRegistered).getTimeInMillis()));
        log.info(customer.getDateRegistered());

        assertEquals("date of last order s/b", "20120528010101", dtf.format(customer.getDateOfLastOrder()));
        assertEquals("date registration s/b", dateRegistered, df.format(customer.getDateRegistered()));

        session.insert(customer);

        Customer customer2 = new Customer();
        customer2.setCustomerId(customer.getCustomerId());

        session.fetch(customer2);

        if (connectionType == ConnectionType.SQLite || connectionType == ConnectionType.Oracle) {
            // SQLite does not support DATE on it's own
            // Oracle DATE also includes time so JDBC driver reports it as timestamp
        } else {
            // Checking to see if time part is removed when using 'DATE' column to LocalDateTime
            assertEquals("date of last order s/b", dateOfLastOrder, dtf.format(customer2.getDateOfLastOrder()));
        }

        assertEquals("date registration s/b", dateRegistered, df.format(customer2.getDateRegistered()));

        // todo Try other param types - convert?
        LocalDate today = LocalDate.now();
        session.query(Customer.class, where("TestLocalDate between ? AND ?"), params(today.minus(1, ChronoUnit.DAYS), today.plus(1, ChronoUnit.DAYS)));

        LocalDateTime dt = LocalDateTime.now();
        //session.query(Customer.class, where("DATE_OF_LAST_ORDER between ? AND ?"), params(dt.minus(1, ChronoUnit.DAYS), dt.plus(1, ChronoUnit.DAYS)));
        // todo fails with postgresql check if converter will fix this.... Where the status col = char(1) and we pass int. No we cant correct for this.
        // session.query(Customer.class, where(":dateOfLastOrder is not null AND :dateOfLastOrder between ? AND ? and :status = ?"), params(dt.minus(1, ChronoUnit.DAYS), dt.plus(1, ChronoUnit.DAYS), 1));
        session.query(Customer.class, where(":dateOfLastOrder is not null AND :dateOfLastOrder between ? AND ? and :status = ?"), params(dt.minus(1, ChronoUnit.DAYS), dt.plus(1, ChronoUnit.DAYS), "1"));

        session.query(Customer.class, where("TestLocalDate between ? AND ?"), params(dt.minus(1, ChronoUnit.DAYS), dt.plus(1, ChronoUnit.DAYS)));

        // MSSQL/JTDS errors with The data types time and datetime are incompatible in the greater than or equal to operator.
        // add ;sendTimeAsDateTime=false to connection string
        // https://stackoverflow.com/questions/38954422/the-data-types-time-and-datetime-are-incompatible-in-the-greater-than-or-equal-t
        // Doesn't work at all with JTDS
        if (connectionType != ConnectionType.JTDS) {
            LocalTime time = LocalTime.now();
            session.query(Contact.class,
                    where(":whatMiteIsIt between ? AND ?"),
                    params(time.minus(10, ChronoUnit.MINUTES), time));
        }
    }

    public void testStoredProcs() {
        // todo testStoredProcs?
    }

    public void testParameters() throws Exception {
    }


    public void testRefreshObject() {

        try {
            log.info("testRefreshObject with : " + con.getMetaData().getURL());

            Customer customer1 = new Customer();
            customer1.setCompanyName("TEST");
            customer1.setCustomerId("MOO");
            customer1.setAddress("123 sesame street");
            customer1.setCity("city");
            customer1.setContactName("fred flintstone");
            customer1.setContactTitle("Lord");
            customer1.setCountry("US");
            customer1.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));
            customer1.setFax("123-456-7890");
            customer1.setPhone("456-678-1234");
            customer1.setPostalCode("54321");
            //customer1.setRegion(Regions.East);
            customer1.setStatus('2');

            session.delete(customer1); // in case it's already there.
            session.insert(customer1);

            String id = customer1.getCustomerId();

            Customer customer2 = new Customer();
            customer2.setCustomerId(id);
            session.fetch(customer2);

            assertNotNull("cust should be found ", customer2);

            long dateRegistered = customer1.getDateRegistered().getTime();

            customer1.setCountry("CA");
            customer1.setDateRegistered(null);

            assertEquals("Customer 1 country should be CA ", "CA", customer1.getCountry());
            assertNull("Customer 1 date registered should be null", customer1.getDateRegistered());

            session.fetch(customer1);

            assertEquals("Customer 1 country should be US ", "US", customer1.getCountry());
            // we cannot test long. Need to format a date and compare as string to the seconds or minutes because SQL does not store dates with exact accuracy
            log.info(new Date(dateRegistered) + " = ? " + new Date(customer1.getDateRegistered().getTime()));
            assertEquals("Customer 1 date registered should be more or less equal since SQL can be off by 7 millis.?", "" + new Date(dateRegistered), "" + new Date(customer1.getDateRegistered().getTime()));

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testQueryWithSpecificColumnsWhereCaseDoesNotMatch() throws SQLException {

        log.info("testQueryWithSpecificColumnsWhereCaseDoesNotMatch with : " + con.getMetaData().getURL());

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        customer.setCountry("US");
        customer.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Region.East);
        customer.setStatus('1');

        session.delete(customer); // i case it already exists.
        session.insert(customer);

        customer.setRegion(Region.North);
        session.update(customer);

        boolean failOnMissingProperties = false;

        try {
            session.query(Customer.class, sql("SELECT Country, PHONE from Customers"), none());
        } catch (Exception e) {
            log.info(e.getMessage());
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        String sql = """
                SELECT company_NAME, GrOUP_id, Date_Of_Last_ORDER, contact_title, pHone, rEGion, postal_CODE, FAX, DATE_Registered,
                ADDress, CUStomer_id, Contact_name, country, city, STATUS, TestLocalDate, TestLocalDateTime
                from Customers
                """;

        List<Customer> list = session.query(Customer.class, sql(sql), none());

        log.info(list);
        assertEquals("list should be 1", 1, list.size());

        Customer c2 = list.get(0);
        assertEquals("region s/b north ", Region.North, c2.getRegion());

        // test util date as param
        session.query(Customer.class, where("DATE_REGISTERED = ?"), params(new java.util.Date(customer.getDateRegistered().getTime())));
    }

    public void testQueryResult() throws Exception {
        queryDataSetup();

        // Create a query that will fail with not enough columns.
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT c.Customer_ID, c.Company_Name");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        String sql = sb.toString();
        log.info(sql);


        List<CustomerOrder> results;
        boolean fail = false;

        try {
            results = session.query(CustomerOrder.class, sql(sql), none());
        } catch (Exception e) {
            fail = true;
            log.info("SHOULD ERROR HERE NOT ENOUGH COLUMNS " + e.getMessage());
        }
        assertTrue(fail);

        sb = new StringBuilder();
        sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.Created AS DateCreated, o.PAID ");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        sql = sb.toString();
        sql += " WHERE 1 = ?";
        log.info(sql);

        // test a fetch too
        var co2 = session.fetch(CustomerOrder.class, sql(sql), params(1));
        assertNotNull(co2);

        // This should not fail - we will refresh the metadata
        results = session.query(CustomerOrder.class, sql(sql), params(1));
        log.info(results);
        assertEquals("size should be 4", 4, results.size());

        // get one for test later with fetch
        CustomerOrder customerOrder = results.get(0);

        // ORDER 1 s/b paid = true others paid = false
        for (CustomerOrder co : results) {
            log.info("date created? " + co.getDateCreated());
            log.info("date paid? " + co.getDatePaid());
            if ("ORDER 1".equals(co.getDescription())) {
                assertTrue("order 1 s/b paid", co.isPaid());
            } else {
                assertFalse("order OTHER s/b NOT paid", co.isPaid());
            }
        }

        Customer customer = new Customer();
        customer.setCustomerId("1234");
        customer.setContactName("Fred");
        customer.setCompanyName("Slate Quarry");
        customer.setRegion(Region.East);
        customer.setStatus('1');
        customer.setAddress("123 Sesame Street");

        session.insert(customer);

        List<Customer> customers = session.query(Customer.class);

        Customer customer1 = session.fetch(Customer.class, params(customers.get(0).getCustomerId()));
        log.info(customer1);

        // test primitives
        String result = session.fetch(String.class, sql("select Contact_Name from Customers where Customer_ID = ?"), params("1234"));
        log.info(result);
        assertEquals("should be Fred", "Fred", result);

        Integer count = session.fetch(Integer.class, sql("select count(*) from Customers where Region = ?"), params(Region.East));
        log.info("count " + count);
        assertEquals("should be 1", "1", "" + count);

        // Arbitrary object query for table non in db.
        messageTester("Could not determine a table for type: java.util.logging.Logger Guesses were: [Logger, Loggers]",
                () -> session.query(Logger.class));

        messageTester("class net.sf.persism.dao.CustomerOrder: QUERY w/o specifying the SQL operation not supported for @NotTable classes",
                () -> session.query(CustomerOrder.class, params("junk")));

        // Test Fetch on NotTable with no SQL provided
        messageTester("class net.sf.persism.dao.CustomerOrder: FETCH w/o specifying the SQL operation not supported for @NotTable classes",
                () -> session.fetch(CustomerOrder.class, params("junk")));

        // Test simple object Fetch with NotTable
        messageTester("class net.sf.persism.dao.CustomerOrder: FETCH operation not supported for @NotTable classes",
                () -> session.fetch(customerOrder));

        messageTester("class net.sf.persism.dao.CustomerInvoice: FETCH operation not supported for Views", () -> session.fetch(new CustomerInvoice()));

        messageTester("WHERE clause not supported for Queries (using @NotTable). If this is a View annotate the class as @View", () -> session.fetch(CustomerOrder.class, where("1=1")));

        List<CustomerRec> customerRecs = session.query(CustomerRec.class);
        assertTrue(customerRecs.size() > 0);

        CustomerRec crec = session.fetch(CustomerRec.class, params(customerRecs.get(0).customerId()));
        assertNotNull(crec);

        CustomerRec crec2 = new CustomerRec('x', crec.customerId(), crec.companyName(), crec.contactName());
        Result<CustomerRec> res = session.update(crec2);
        log.warn(res);
    }

    public void testJoinsCustomer() {
        queryDataSetup();

        var customer = session.fetch(Customer.class, where(":customerId = ?"), params("123"));
        assertNotNull(customer);
        System.out.println("***********************************************************");
        var customers = session.query(Customer.class, where(":customerId IS NOT NULL"));
        assertTrue(customers.size() > 0);
    }

    public void testJoinsParentFetch() throws SQLException {

        // todo test where column names are different between classes
        // todo test adding a Join to a String or int property etc... Should fail spectacularly

        queryDataSetup();

        // reuse params object - it should not be modified
        var params = params("123");
        var customer = session.fetch(Customer.class, where(":customerId = ?"), params);
        assertNotNull(customer);

        var invoices = customer.getInvoices();
        assertEquals(2, invoices.size());

        // reuse params
        var customerRec = session.fetch(CustomerRec.class, where(":customerId = ?"), params);
        assertNotNull(customerRec);

        var invoices2 = customerRec.invoices();
        assertEquals(2, invoices2.size());


        InvoiceLineItem invoiceLineItem = session.fetch(InvoiceLineItem.class, params(1));
        log.info(invoiceLineItem);

        assertNotNull(invoiceLineItem);
        assertNotNull(invoiceLineItem.getProduct());

        // this should fail miserably as we can't set fields on records
        messageTester("Can not set final net.sf.persism.dao.Product field net.sf.persism.dao.records.InvoiceLineItemRec.product to net.sf.persism.dao.Product",
                () -> session.fetch(InvoiceLineItemRec.class, params(1)));
    }

    public void testJoinsParentQuery() throws SQLException {
        queryDataSetup();

        var list1 = session.query(Customer.class, where(":status = ? and :groupId = ?"), params('1', 0));

        assertEquals(2, list1.size());
        assertEquals(2, list1.get(0).getInvoices().size());
        assertEquals(0, list1.get(1).getInvoices().size());

        var list2 = session.query(CustomerRec.class);

        assertEquals(2, list2.size());
        assertEquals(2, list2.get(0).invoices().size());
        assertEquals(0, list2.get(1).invoices().size());
        var invoices = list1.get(0).getInvoices();
        var invoice = invoices.stream().filter(invoice1 -> invoice1.getInvoiceId() == 1).findFirst().get();
        assertNotNull(invoice.getLineItems().get(0).getProduct());
    }

    public void testUnknownConnectionType() throws Exception {
        Class.forName("org.xbib.jdbc.csv.CsvDriver");

        var con = DriverManager.getConnection("jdbc:xbib:csv:" + System.getProperty("user.home"));
        Session session2 = new Session(con);
        assertNotNull(session2);
    }

    public void testSelectMultipleByPrimaryKey() throws SQLException {
        queryDataSetup();
        List<Order> orders = session.query(Order.class);
        log.info(orders);

        assertEquals("should be 4 ", 4, orders.size());
        orders = session.query(Order.class, params(1, 4));

        log.info(orders);

        assertEquals("should be 2 ", 2, orders.size());

        orders = session.query(Order.class, params(2, 3));
        assertEquals("should be 2 ", 2, orders.size());

        orders = session.query(Order.class, params(2, 3, 1, 4));
        assertEquals("should be 4 ", 4, orders.size());

    }

    public void testQueryResultRecord() throws Exception {
        String sql;
        StringBuilder sb;
        List<CustomerOrderRec> results;

        log.error("testQueryResultRecord*************************************");
        queryDataSetup();

        sb = new StringBuilder();
        sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.Created AS DateCreated, o.PAID ");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        sql = sb.toString();
        log.info(sql);

        results = session.query(CustomerOrderRec.class, sql(sql));
        log.info(results);
        assertEquals("size should be 4", 4, results.size());

        Constructor<?> c = getCanonicalConstructor(CustomerOrderRec.class);
        assertNotNull(c);
        log.info("RECORD: " + debugConstructor(c));

        c = getCanonicalConstructor(CustomerOrder.class);
        assertNull(c); // should be null - this is not a record
        log.info("OBJECT: " + debugConstructor(c));

        CustomerOrderRec cor = new CustomerOrderRec("1", "name", "desc", 123L, LocalDateTime.now(), null, false);
        log.info(cor);

        // ORDER 1 s/b paid = true others paid = false
        for (CustomerOrderRec customerOrder : results) {
            log.info("date created? " + customerOrder.dateCreated());
            log.info("date paid? " + customerOrder.datePaid());
            if ("ORDER 1".equals(customerOrder.description())) {
                assertTrue("order 1 s/b paid", customerOrder.paid());
            } else {
                assertFalse("order OTHER s/b NOT paid", customerOrder.paid());
            }
        }

        // should fail? missing column? - removed alias o.Created AS DateCreated
        // No, we added a constructor for it. See RecordTest1 for fail case

        sb = new StringBuilder();

        sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.PAID ");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        sql = sb.toString();
        log.info("testQueryResultRecord: " + sql);

        var fail = session.query(CustomerOrderRec.class, sql(sql));
        log.warn("testQueryResultRecord: " + fail);

    }

    public void testQueryResultRecordNegative() throws Exception {
        log.error("testQueryResultRecordNegative");
        queryDataSetup();

        var sb = new StringBuilder();

        sb.append("SELECT c.Customer_ID");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        var sql = sb.toString();
        log.info(sql);

        boolean failed = false;

        try {
            session.query(CustomerOrderGarbage.class, sql(sql));
        } catch (PersismException e) {
            // should fail since there are other properties on CustomerOrderGarbage not referenced by the query AND we don't do anything with @NotColumn
            log.error(e.getMessage(), e);
            assertEquals("s/b eq", CouldNotFindConstructorForRecord.message(CustomerOrderGarbage.class.getName(), "[customerId]"), e.getMessage());
            failed = true;
        }
        // This will fail if we compile with -parameters
        assertTrue(failed);

        sb = new StringBuilder();
        sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.PAID ");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        sql = sb.toString();
        log.info(sql);

        failed = false;
        // should fail will no appropriate constructor
        // DOESNT FAIL - IGNORES ALL OTHER COLUMNS. WTF
        try {
            session.query(CustomerOrderGarbage.class, sql(sql));
        } catch (PersismException e) {
            // should fail since there are other properties on CustomerOrderGarbage not referenced by the query AND we don't do anything with @NotColumn
            // AND we can't match these property names
            log.error(e.getMessage(), e);
            assertEquals("s/b eq", CouldNotFindConstructorForRecord.message(CustomerOrderGarbage.class.getName(), "[customerId]"), e.getMessage());
            failed = true;
        }
        assertTrue(failed);
    }

    public void testMessages() throws Exception {
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();
        String sql;
        boolean fail;

        Product product = new Product(4, "test", 10.00);
        product.setBadNumber(new BigDecimal("10"));
        session.insert(product);

        session.query(Product.class); // should work

        var info = session.metaData.getTableInfo(Product.class);
        // set this to something junk
        sql = "UPDATE " + info.name() + " SET BADNUMBER=? WHERE ID=?";
        session.helper.execute(sql, "NAN JUNK", product.getId());

        fail = false;
        // should fail with NumberFormatException
        try {
            session.query(Product.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail = true;
            assertEquals("s/b number format exception",
                    NumberFormatException.message("BADNUMBER", BigDecimal.class, String.class, "NAN JUNK").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(fail);

        sql = "UPDATE " + info.name() + " SET BADDATE=?, BADNUMBER=? WHERE ID=?";
        session.helper.execute(sql, "NAD JUNK", "0", product.getId());

        fail = false;
        // should fail with DateFormatException
        try {
            session.query(Product.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail = true;
            assertEquals("s/b date format exception",
                    DateFormatException.message("Unparseable date: \"NAD JUNK\"", "BADDATE",
                            java.util.Date.class, String.class, "NAD JUNK").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(fail);

        sql = "UPDATE " + info.name() + " SET BADTIMESTAMP=?, BADDATE=?, BADNUMBER=? WHERE ID=?";
        session.helper.execute(sql, "NAD JUNK", null, "0", product.getId());

        fail = false;
        // should fail with DateFormatException
        try {
            session.query(Product.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail = true;
            assertEquals("s/b date format exception",
                    DateFormatException.message("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]", "BADTIMESTAMP", Timestamp.class, String.class, "NAD JUNK").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(fail);

        // throw new PersismException(Messages.ReadRecordColumnNotFound.message(objectClass, col));

        RecordTest2 rt2 = new RecordTest2(0, "test 1", 10, 3.99, LocalDateTime.now());
        session.insert(rt2);

        fail = false;
        // should fail with ReadRecordColumnNotFound
        try {
            session.query(RecordTest2.class, SQL.sql("select description, qty, price FROM RecordTest2"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail = true;
            assertEquals("s/b ReadRecordColumnNotFound exception",
                    ReadRecordColumnNotFound.message(RecordTest2.class, "ID").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(fail);

        // TableHasNoPrimaryKeys
        TableInfo table = session.metaData.getTableInfo(CorporateHoliday.class);
        CorporateHoliday holiday = new CorporateHoliday("-99", "blah", LocalDate.now());
        session.insert(holiday);

        messageTester(TableHasNoPrimaryKeys.message("FETCH", table.name()), () -> session.fetch(holiday));

        messageTester(TableHasNoPrimaryKeysForWhere.message(table.name()), () -> session.fetch(CorporateHoliday.class, params(1, 2, 3)));

        messageTester("class net.sf.persism.dao.CustomerInvoice: FETCH w/o specifying the SQL with @View operation not supported for Views", () -> session.fetch(CustomerInvoice.class, params(1, 2, 3)));

        messageTester("class java.lang.String: QUERY w/o specifying the SQL operation not supported for Java types", () -> session.query(String.class));

        messageTester("class java.lang.String: QUERY operation not supported for Java types", () -> session.query(String.class, params(1, 2, 3)));

        var tableInfo = session.metaData.getTableInfo(TableNoPrimary.class);
        messageTester("Cannot perform QUERY - " + tableInfo + " has no primary keys", () -> session.query(TableNoPrimary.class, params(1, 2, 3)));

        messageTester("Cannot perform DELETE - " + tableInfo + " has no primary keys", () -> session.delete(TableNoPrimary.class, params(1, 2, 3)));

        if (connectionType != ConnectionType.Informix) {
            // Informix doesn't allow a manually specified primary on the POJO
            // Error In Specifying Automatically (Server) Generated Keys.
            fail = false;
            Postman postman = null;
            try {
                postman = new Postman().host("host").port(1).user("dan").password("123");
                session.insert(postman);
            } catch (PersismException e) {
                log.error(e.getMessage(), e);
                assertEquals("s/b EQUAL ", ClassHasNoGetterForProperty.message(Postman.class, "missingGetter"), e.getMessage());
                fail = true;
            }
            assertTrue(fail);

            fail = false;
            // insert has the check - so we'll insert outside and test query
            sql = session.metaData.getInsertStatement(postman, con);
            System.out.println(sql);
            session.helper.execute(sql, "host", 1, "dan", "123", 456);
            try {
                session.query(Postman.class);
            } catch (PersismException e) {
                log.error(e.getMessage(), e);
                assertEquals("s/b EQUAL ", ClassHasNoGetterForProperty.message(Postman.class, "missingGetter"), e.getMessage());
                fail = true;
            }
            assertTrue(fail);
        }

        if (connectionType.supportsNonAutoIncGenerated()) {
            CustomerFail customer = new CustomerFail();
            customer.setCompanyName("abc inc");
            session.insert(customer);
            System.out.println("ASS! *********** " + customer.customerId());
            assertNotNull(customer.customerId());
        } else {
            fail = false;
            try {
                CustomerFail customer = new CustomerFail();
                customer.setCompanyName("abc inc");
                session.insert(customer);
            } catch (PersismException e) {
                log.error(e.getMessage(), e);
                assertEquals("s/b EQUAL ", NonAutoIncGeneratedNotSupported.message(), e.getMessage());
                fail = true;
            }
            assertTrue(fail);
        }

        fail = false;
        try {
            CustomerFail2 customer2 = new CustomerFail2();
            customer2.setCompanyName("abc inc");
            session.insert(customer2);
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ", CouldNotFindTableNameInTheDatabase.message("CustomerTABLEDOESNTEXIST", CustomerFail2.class.getName()), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.query(CustomerInvoiceFail.class);
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ", CouldNotFindViewNameInTheDatabase.message("NOVIEWNAMEDCustomerInvoiceFail", CustomerInvoiceFail.class.getName()), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.query(CustomerInvoiceFail2.class);
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ",
                    CouldNotDetermineTableOrViewForType.message("view",
                            CustomerInvoiceFail2.class.getName(),
                            "[CustomerInvoiceFail2, CustomerInvoiceFail2s, Customer Invoice Fail2, Customer_Invoice_Fail2, Customer Invoice Fail2s, Customer_Invoice_Fail2s]"),
                    e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.fetch("");
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ",
                    OperationNotSupportedForJavaType.message(String.class, "FETCH"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.fetch(new CustomerRec('a', "123", "name", "contact"));
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ",
                    OperationNotSupportedForRecord.message(CustomerRec.class, "FETCH"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        queryDataSetup();

        fail = false;
        try {
            session.query(InvoiceFail2.class);
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ",
                    CannotNotJoinToNullProperty.message("lineItems"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.query(InvoiceFail3.class);
        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            assertEquals("s/b EQUAL ",
                    PropertyNotFoundForJoin.message("invoice0d", InvoiceFail3.class), e.getMessage());
            fail = true;
        }
        assertTrue(fail);


        // Create 2 tables to match on table search which should fail
        if (session.metaData.getConnectionType().supportsSpacesInTableNames()) {
            fail = false;
            try {

                session.query(MultiMatch.class);

            } catch (PersismException e) {
                fail = true;

                String message = CouldNotDetermineTableOrViewForTypeMultipleMatches.
                        message("table", MultiMatch.class.getName(),
                                "[multimatch, multimatchs, multi match, multi_match, multi matchs, multi_matchs]",
                                "[Multi match, MultiMatch]").toLowerCase();

                assertEquals("S/B equal", message, e.getMessage().toLowerCase());
            }
            assertTrue(fail);
        }
    }

    private void createMultiMatch(String tableName, ConnectionType connectionType) throws SQLException {
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        String sql;
        if (isTableInDatabase(connectionType.getSchemaPattern(), tableName, con)) {
            executeCommand("DROP TABLE " + sd + tableName + ed, con);
        }
        sql = "CREATE TABLE " + sd + tableName + ed + "(\n" +
              "    ID int ,\n" +
              "    Name VARCHAR(10) \n" +
              "    )\n";
        executeCommand(sql, con);
    }


    public void testTableNoPrimary() {
        TableNoPrimary junk = new TableNoPrimary();
        junk.setId(1);
        junk.setName("JUNK");

        // This should work OK
        session.insert(junk);

        log.info(session.query(TableNoPrimary.class, sql("SELECT * FROM " + session.metaData.getTableInfo(TableNoPrimary.class).name())));

        boolean shouldFail = false;

        junk.setName("NO WORKEE!");
        try {
            session.update(junk);
        } catch (PersismException e) {
            shouldFail = true;
            assertEquals("Message s/b eq",
                    Message.TableHasNoPrimaryKeys.message("UPDATE", "TableNoPrimary").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(shouldFail);

        shouldFail = false;
        try {
            session.fetch(junk);
        } catch (PersismException e) {
            shouldFail = true;
            assertEquals("Message s/b eq",
                    Message.TableHasNoPrimaryKeys.message("FETCH", "TableNoPrimary").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(shouldFail);

        shouldFail = false;
        try {
            session.delete(junk);
        } catch (PersismException e) {
            shouldFail = true;
            assertEquals("Message s/b eq",
                    Message.TableHasNoPrimaryKeys.message("DELETE", "TableNoPrimary").toLowerCase(),
                    e.getMessage().toLowerCase());
        }
        assertTrue(shouldFail);
    }

    final void queryDataSetup() {

        Invoice invoice1 = new Invoice();
        invoice1.setCustomerId("123");
        invoice1.setPaid(true);
        invoice1.setPrice(10.0f);
        invoice1.setActualPrice(BigDecimal.TEN);
        invoice1.setQuantity(10);
        invoice1.setStatus('1');
        session.insert(invoice1);
        assertTrue(invoice1.getInvoiceId() > 0);

        Invoice invoice2 = new Invoice();
        invoice2.setCustomerId("123");
        invoice2.setPaid(true);
        invoice2.setPrice(20.0f);
        invoice2.setActualPrice(BigDecimal.valueOf(20));
        invoice2.setQuantity(20);
        invoice2.setStatus('1');
        session.insert(invoice2);

        Product product;
        product = new Product(1, "prod 1", 10.25);
        session.insert(product);

        product = new Product(2, "prod 2", 17.25);
        session.insert(product);

        product = new Product(3, "prod 3", 9.75);
        session.insert(product);

        InvoiceLineItem invoiceLineItem;
        invoiceLineItem = new InvoiceLineItem(invoice1.getInvoiceId(), 1, 10);
        session.insert(invoiceLineItem);
        assertTrue(invoiceLineItem.getId() > 0);

        invoiceLineItem = new InvoiceLineItem(invoice1.getInvoiceId(), 2, 20);
        session.insert(invoiceLineItem);
        assertTrue(invoiceLineItem.getId() > 0);

        invoiceLineItem = new InvoiceLineItem(invoice1.getInvoiceId(), 3, 5);
        session.insert(invoiceLineItem);
        assertTrue(invoiceLineItem.getId() > 0);
// todo bad test we need more lineitems to multiple products - bug where we loop child and set the product only on 1 parent rather than any parent referencing that product.

        Customer c1 = new Customer();
        c1.setCustomerId("123");
        c1.setCompanyName("ABC INC");
        c1.setStatus('1');
        session.insert(c1);

        Customer c2 = new Customer();
        c2.setCustomerId("456");
        c2.setCompanyName("XYZ INC");
        c2.setStatus('1');
        session.insert(c2);

        Order order;
        order = DAOFactory.newOrder(con);
        order.setCustomerId("123");
        order.setName("ORDER 1");
        order.setCreated(LocalDate.now());
        order.setDatePaid(LocalDateTime.now());

        order.setPaid(true);
        session.insert(order);

        assertTrue("order # > 0", order.getId() > 0);

        session.fetch(order);

        List<Order> orders = session.query(Order.class, sql("select * from Orders"));
        assertEquals("should have 1 order", 1, orders.size());
        assertTrue("order id s/b > 0", orders.get(0).getId() > 0);

        order = DAOFactory.newOrder(con);
        order.setCustomerId("123");
        order.setName("ORDER 2");
        order.setCreated(LocalDate.now());
        session.insert(order);

        order = DAOFactory.newOrder(con);
        order.setCustomerId("456");
        order.setName("ORDER 3");
        order.setCreated(LocalDate.now());
        session.insert(order);

        order = DAOFactory.newOrder(con);
        order.setCustomerId("456");
        order.setName("ORDER 4");
        order.setCreated(LocalDate.now());
        session.insert(order);

        log.info("done");
    }


    public void testReadPrimitive() throws SQLException {

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        customer.setCountry("US");
        customer.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Region.East);
        customer.setStatus('2');


        session.delete(customer); // in case it already exists.
        session.insert(customer);

        List<String> list = session.query(String.class, sql("SELECT Country from Customers"));


        Order order;
        order = DAOFactory.newOrder(con);
        order.setCustomerId("MOO");
        order.setName("ORDER 1");
        //order.setCreated(new java.sql.Date(System.currentTimeMillis()));
        order.setPaid(true);
        session.insert(order);

        assertTrue("order # > 0", order.getId() > 0);
        assertNotNull("order created date should be defaulted", order.getCreated());


        assertEquals("list should have 1", 1, list.size());
        assertEquals("String should be US", "US", list.get(0));

        String countryString = session.fetch(String.class, sql("SELECT Country from Customers"), none());
        assertEquals("String should be US", "US", countryString);

        countryString = "NOT US";
        countryString = session.fetch(String.class, sql("SELECT Country from Customers"), none());
        assertEquals("String should be US", "US", countryString);

        List<Date> dates = session.query(Date.class, sql("select Date_Registered from Customers "));
        log.info(dates);

        Date dt = session.fetch(Date.class, sql("select Date_Registered from Customers "), none());
        log.info(dt);

        // Fails because there is no way to instantiate java.sql.Date - no default constructor.
        List<java.sql.Date> sdates = session.query(java.sql.Date.class, sql("select Date_Registered from Customers "));
        log.info(sdates);

        java.sql.Date sdt = session.fetch(java.sql.Date.class, sql("select Date_Registered from Customers "), none());
        log.info(sdt);

        // this should fail. We can't do simple read on a primitive
        boolean failed = false;
        try {
            session.fetch(countryString);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'class java.lang.String: FETCH operation not supported for Java types'",
                    "class java.lang.String: FETCH operation not supported for Java types",
                    e.getMessage());
        }
        assertTrue("should have thrown the exception", failed);

        failed = false;
        try {
            session.fetch(String.class, params(""));
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'class java.lang.String: FETCH operation not supported for Java types'",
                    "class java.lang.String: FETCH operation not supported for Java types",
                    e.getMessage());
        }
        assertTrue("should have thrown the exception", failed);
    }

    UUID identity = UUID.fromString(UUID1);
    UUID partnerId = UUID.fromString(UUID2);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    static SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    LocalDateTime ldt1 = LocalDateTime.parse("1998-02-17 10:23:43.567", formatter);
    LocalDateTime ldt2 = LocalDateTime.parse("1997-02-17 10:23:43.123", formatter);
    LocalDateTime ldt3 = LocalDateTime.parse("1996-02-17 10:23:52.678", formatter);
    LocalDateTime ldt4 = LocalDateTime.parse("1994-02-17 10:23:43.997", formatter);
    java.util.Date date = Timestamp.valueOf("1992-02-17 10:23:41.107");

    Contact getContactForTest() {

        Contact contact = new Contact();
        contact.setIdentity(identity);
        contact.setPartnerId(partnerId);
        contact.setFirstname("Fred");
        contact.setLastname("Flintstone");
        contact.setDivision("DIVISION X");
        contact.setContactName("Fred Flintstone");
        contact.setAddress1("123 Sesame Street");
        contact.setAddress2("Appt #0 (garbage can)");
        contact.setCompany("Grouch Inc");
        contact.setCountry("US");
        contact.setCity("Philly?");
        contact.setType("X");
        contact.setAmountOwed(100.23f);
        contact.setNotes("B:AH B:AH VBLAH\r\n BLAH BLAY!");
        contact.setStatus((byte) 10);

        contact.setDateAdded(Date.valueOf(ldt1.toLocalDate()));
        contact.setLastModified(Timestamp.valueOf(ldt2));
        contact.setWhatTimeIsIt(Time.valueOf(ldt3.toLocalTime()));
        contact.setWhatMiteIsIt(contact.getWhatTimeIsIt().toLocalTime());
        contact.setSomeDate(date);
        return contact;
    }

    public void testCustomerInvoiceView() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerId("ABC");
        customer.setCompanyName("ABC Inc");
        session.insert(customer);
        Invoice invoice = new Invoice();
        invoice.setCustomerId("ABC");
        invoice.setQuantity(10);
        invoice.setPrice(10.23f);
        invoice.setActualPrice(BigDecimal.valueOf(9.99d));
        invoice.setStatus((char) 1);
        session.insert(invoice);
        Customer customer1 = session.fetch(Customer.class, sql("SELECT * FROM Customers WHERE Company_Name = ?"), params("ABC Inc"));
        assertNotNull(customer1);
        CustomerInvoice customerInvoice = session.fetch(CustomerInvoice.class, sql("SELECT * FROM CustomerInvoice WHERE Company_Name = ?"), params("ABC Inc"));
        assertNotNull(customerInvoice);

        CustomerInvoiceRec customerInvoiceRec = session.fetch(CustomerInvoiceRec.class, sql("SELECT * FROM CustomerInvoice WHERE Company_Name = ?"), params("ABC Inc"));
        assertNotNull(customerInvoiceRec);

        CustomerInvoiceTestView customerInvoiceTestView = session.fetch(CustomerInvoiceTestView.class, sql("SELECT * FROM CustomerInvoice WHERE Company_Name = ?"), params("ABC Inc"));
        List<CustomerInvoiceTestView> list2 = session.query(CustomerInvoiceTestView.class);

        assertNotNull(customerInvoiceTestView);
        assertTrue(list2.size() > 0);


        if (connectionType.supportsNonAutoIncGenerated()) {
            Customer customer2 = new Customer();
            customer.setCompanyName("abc inc");
            session.insert(customer2);
            assertNotNull(customer2.getCustomerId());
        }

        boolean fail = false;
        try {
            session.insert(customerInvoiceTestView);
        } catch (PersismException e) {
            fail = true;
            assertEquals("s/b", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "INSERT"), e.getMessage());
        }
        assertTrue(fail);

        fail = false;
        try {
            session.update(customerInvoiceTestView);
        } catch (PersismException e) {
            fail = true;
            assertEquals("s/b", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "UPDATE"), e.getMessage());
        }
        assertTrue(fail);

        fail = false;
        try {
            session.delete(customerInvoiceTestView);
        } catch (PersismException e) {
            fail = true;
            assertEquals("s/b", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "DELETE"), e.getMessage());
        }
        assertTrue(fail);

        fail = false;
        try {
            // @NotTable Should fail. We can't use no sql specified for these since we don't know what the SQL would be.
            List<CustomerInvoiceResult> list3 = session.query(CustomerInvoiceResult.class);
        } catch (PersismException e) {
            log.info(e);

            fail = true;
            assertEquals("s/b",
                    Message.OperationNotSupportedForNotTableQuery.message(CustomerInvoiceResult.class, "QUERY w/o specifying the SQL"),
                    e.getMessage());
        }
        assertTrue(fail);

        customer = new Customer();
        customer.setCustomerId("XYZ");
        customer.setCompanyName("XYZ Inc");
        session.insert(customer);

        invoice = new Invoice();
        invoice.setCustomerId("XYZ");
        invoice.setQuantity(10);
        invoice.setPrice(10.23f);
        invoice.setActualPrice(BigDecimal.valueOf(9.99d));
        invoice.setStatus((char) 1);
        session.insert(invoice);

        session.fetch(CustomerInvoice.class, where(":companyName = ?"), params("ABC Inc"));
        session.query(CustomerInvoice.class);
        session.query(CustomerInvoice.class, where(":companyName = ?"), params("ABC Inc"));
        session.query(CustomerInvoice.class, sql("SELECT * FROM CustomerInvoice"));

        customerInvoiceTestView = session.fetch(CustomerInvoiceTestView.class, where(":companyName = ?"), params("ABC Inc"));
        session.query(CustomerInvoiceTestView.class);
        session.query(CustomerInvoiceTestView.class, where(":companyName = ?"), params("ABC Inc"));
        session.query(CustomerInvoiceTestView.class, sql("SELECT * FROM CustomerInvoice"));

        assertNotNull(customerInvoiceTestView);

        fail = false;
        try {
            session.insert(customerInvoiceTestView); // not supported error
        } catch (PersismException e) {
            log.info(e.getMessage());
            assertEquals("s/b Operation not supported for Views.", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "INSERT"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.update(customerInvoiceTestView);
        } catch (PersismException e) {
            log.info(e.getMessage());
            assertEquals("s/b Operation not supported for Views.", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "UPDATE"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.delete(customerInvoiceTestView);
        } catch (PersismException e) {
            log.info(e.getMessage());
            assertEquals("s/b Operation not supported for Views.", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "DELETE"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.insert(customerInvoiceTestView);
        } catch (PersismException e) {
            log.info(e.getMessage());
            assertEquals("s/b Operation not supported for Views.", Message.OperationNotSupportedForView.message(customerInvoiceTestView.getClass(), "INSERT"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        // old call
        List<CustomerInvoiceResult> results = session.query(CustomerInvoiceResult.class, sql("SELECT * FROM CustomerInvoice"));
        log.info(results);

        // todo CustomerInvoiceResult with named parameters YES TODO

        fail = false;
        try {
            // should fail with WHERE clause not supported
            List<CustomerOrder> junk = session.query(CustomerOrder.class, where(":customerId = ?"), params("x"));
        } catch (PersismException e) {
            assertEquals("message should be WHERE clause not supported...", Message.WhereNotSupportedForNotTableQueries.message(), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        session.query(CustomerInvoice.class, none());

        var customerInvoiceView = session.metaData.getTableInfo(CustomerInvoice.class).name();

        String sql = "SELECT * FROM " + customerInvoiceView;

        session.query(CustomerInvoice.class, sql(sql), none());
        sql = "SELECT Customer_ID, Company_Name, Invoice_ID, Status, DateCreated, PAID, Quantity FROM " + customerInvoiceView;

        session.query(CustomerInvoice.class, sql(sql), none());

        // we ARE NOT supporting property names for general SQL. Not really worth it. - YES IT IS! NO IT ISNT!
        fail = false;
        try {
            sql = "SELECT :customerId, :companyName, :invoiceId, :status, :dateCreated, :paid, :quantity FROM " + customerInvoiceView;

            session.query(CustomerInvoice.class, sql(sql), none());

        } catch (PersismException e) {
            // message would be different for different DBS.
            log.info(e.getMessage(), e);
            fail = true;
        }
        assertTrue(fail);
    }

    public void testContactTable() throws SQLException {

        Contact contact = getContactForTest();

        assertNotNull(contact.getIdentity());

        assertEquals("expect 1", 1, session.insert(contact).rows());

        assertNotNull(contact.getIdentity());

        // query back with the identity UUID
        Contact resultX = session.fetch(Contact.class, params(identity));
        assertNotNull(resultX);

        contact.setNotes(null);
        assertEquals("expect 1", 1, session.update(contact).rows());

        Contact contact2 = new Contact();
        contact2.setIdentity(identity);
        assertTrue(session.fetch(contact2));
        assertNotNull(contact2.getPartnerId());
        assertEquals(contact2.getIdentity(), identity);
        assertEquals(contact2.getPartnerId(), partnerId);

        contact2 = new Contact();
        contact2.setIdentity(UUID.fromString(UUID3));
        contact2.setPartnerId(partnerId);
        contact2.setContactName("test 2");
        contact2.setFirstname("wilma");
        contact2.setLastname("flintstone");
        contact2.setCompany("compaty");
        contact2.setType("X");
        session.insert(contact2);

        // test query with primary params
        var list = session.query(Contact.class, params(UUID.fromString(UUID1), UUID.fromString(UUID2), UUID.fromString(UUID3)));
        assertEquals("list should be 2", 2, list.size());
        session.delete(contact2);


        contact.setDivision("Y");
        assertEquals("1 update?", 1, session.update(contact).rows());

        contact.setDivision("Y");
        assertEquals("0 update?", 0, session.update(contact).rows());

        List<Contact> contacts = session.query(Contact.class);
        log.info(contacts);
        assertEquals("should have 1? ", 1, contacts.size());

        Contact contact1 = contacts.get(0);
        log.info("CONTACT: " + contact1);

        // TODO we can't convert parameters that are not primary keys since we don't know for sure which column they may refer to,
        Object param = partnerId;
        if (ConnectionType.Firebird == session.metaData.getConnectionType()) {
            param = Converter.asBytesFromUUID(partnerId);
        }
        contacts = session.query(Contact.class, where(":partnerId = ?"), params(param));
        assertEquals("should have 1", 1, contacts.size());

        assertEquals("1?", 1, session.delete(contact).rows());

        assertEquals("UDDI should be the same ", UUID1, contact1.getIdentity().toString());
        assertEquals("UDDI should be the same ", UUID2, contact1.getPartnerId().toString());

        assertEquals("Date Added sql.Date s/b '1998-02-17'", "1998-02-17", "" + contact1.getDateAdded());

        // MySQL fails with minor accuracy
        // Expected :1997-02-17 10:23:43.123
        // Actual   :1997-02-17 10:23:43.0
        // https://dev.mysql.com/doc/refman/5.7/en/date-and-time-types.html
        // Has the accuracy in v8 so once we update the DB and driver we should retest
        if (connectionType == ConnectionType.MySQL) {
            assertEquals("last modified util.Date s/b '1997-02-17 10:23:43.0'", "1997-02-17 10:23:43.0", "" + contact1.getLastModified());
        } else {
            assertEquals("last modified util.Date s/b '1997-02-17 10:23:43.123'", "1997-02-17 10:23:43.123", "" + contact1.getLastModified());
        }

        assertEquals("what time is it? sql.Time s/b '10:23:52'", "10:23:52", "" + contact1.getWhatTimeIsIt());
        assertEquals("what MITE? is it? LocalTime s/b '10:23:52'", "10:23:52", "" + contact1.getWhatMiteIsIt());

        assertEquals("some date s/b '1992-02-17 10:23:41.107'", "1992-02-17 10:23:41.107", "" + contact.getSomeDate());

        // test transaction

        contact = getContactForTest();
        session.insert(contact);

        boolean shouldFail = false;

        final UUID randomUUID = UUID.randomUUID();
        try {
            session.withTransaction(() -> {
                Contact contactForTest = getContactForTest();
                contactForTest.setIdentity(randomUUID);
                session.insert(contactForTest);
                contactForTest.setContactName("HELLO?!");
                contactForTest.setCompany("some company");
                session.update(contactForTest);
                session.fetch(contactForTest);

                log.info("contact after insert/update before commit/rollback: " + contactForTest);

                // NOW FAIL the transaction to see that the new contact was not committed
                session.query(Object.class, proc("select * FROM TABLE THAT DOESN'T EXIST!!!!"));
            });
        } catch (Exception e) {
            log.info("SHOULD FAIL: " + e);
            shouldFail = true;
        }

        assertTrue(shouldFail);

        // pass one
        session.withTransaction(() -> {
            Contact contactForTest = getContactForTest();
            contactForTest.setIdentity(UUID.randomUUID());
            session.insert(contactForTest);
            contactForTest.setContactName("HELLO?!@");
            session.update(contactForTest);
            session.fetch(contactForTest);

            log.info("contact after insert/update before commit/rollback: " + contactForTest);
        });

        Contact contactForTest = getContactForTest();
        contactForTest.setIdentity(randomUUID);

        assertFalse("Should not be found", session.fetch(contactForTest));

        // null checks for unset properties.
        contact = new Contact();
        UUID rand = UUID.randomUUID();
        contact.setIdentity(rand);
        contact.setPartnerId(partnerId);
        contact.setType("X");
        contact.setFirstname("not null");
        contact.setLastname("not null");
        contact.setCompany("Y");
        contact.setContactName("X");

        session.insert(contact);
        session.fetch(contact);

        boolean failed = false;

        // https://stackoverflow.com/questions/45305283/parsing-sql-query-in-java

        // test where and keys - this at least does conversions for UUID so they don't have to manually do it.
        // As long as they use the query/fetch without the SQL param.
        String columnName = session.getMetaData().getPrimaryKeys(Contact.class, con).get(0);
        String where = session.getMetaData().getConnectionType().getKeywordStartDelimiter() +
                       columnName +
                       session.getMetaData().getConnectionType().getKeywordEndDelimiter() +
                       "=?";
        log.info("testContactTable " + where);
        // testing that this should not fail.
        List<Contact> results = session.query(Contact.class, params(identity));
        log.info(results);

        Contact contactx = session.fetch(Contact.class, params(identity));
        log.info(contactx);
        assertNotNull(contactx);


        var sd = session.getMetaData().getConnectionType().getKeywordStartDelimiter();
        var ed = session.getMetaData().getConnectionType().getKeywordEndDelimiter();
        if (COLUMN_FIRST_NAME.contains(" ")) {
            COLUMN_FIRST_NAME = sd + COLUMN_FIRST_NAME + ed;
        }
        if (COLUMN_LAST_NAME.contains(" ")) {
            COLUMN_LAST_NAME = sd + COLUMN_LAST_NAME + ed;
        }
        // named params
        SQL sql = sql("select * from Contacts where (" + COLUMN_FIRST_NAME + " = @name OR Company = @name) and " + COLUMN_LAST_NAME + " = @last");

        log.info(sql);
        contacts = session.query(Contact.class,
                sql,
                // named(Map.of(null, "junk"))); // fails here with NullPointerException which is fine
                //named(Map.of("", "junk"))); // index out of range - added IF
                //named(Collections.emptyMap())); // index out of range - added IF
                params(Map.of("last", "Flintstone", "name", "Fred"))); // works
        log.info(contacts);

        // named params + properties instead of columns
        sql = where("(:firstname = @name OR :company = @name) and :lastname = @last");
        log.info(sql);
        contacts = session.query(Contact.class,
                sql,
                params(Map.of("name", "Fred", "last", "Flintstone")));
        log.info(contacts);

        // Fetch?
        contact = session.fetch(Contact.class, sql, params(Map.of("name", "Fred", "last", "Flintstone")));
        assertNotNull(contact);

        sql = where("(:firstname = @name OR :company = @name) and :lastname = @last and :city = @city and :amountOwed > @owe ORDER BY :dateAdded");
        log.info(sql);
        contacts = session.query(Contact.class,
                sql,
                params(Map.of("name", "Fred", "last", "Flintstone", "owe", 10, "city", "Somewhere")));
        log.info(contacts);

        // Misspell parameters
        sql = where("(:firstname = @name OR :company = @name) and :lastname = @last and :city = @city and :amountOwed > @owe");
        log.info(sql);
        try {
            contacts = session.query(Contact.class,
                    sql,
                    params(Map.of("Xame", "Fred", "Xast", "Flintstone", "owe", 10, "city", "Somewhere")));
        } catch (PersismException e) {
            failed = true;
            log.info(e.getMessage(), e);
            String msg = Message.QueryParameterNamesMissingOrNotFound.message("[last, name]", "[Xame, Xast]");
            assertEquals("s/b " + msg, msg, e.getMessage());
        }
        assertTrue(failed);

        // Misspelled property names
        failed = false;
        sql = where("(:firstXame = @name OR :Xompany = @name) and :lastname = @last and :city = @city and :amountOwed > @owe");
        log.info(sql);
        try {
            contacts = session.query(Contact.class,
                    sql,
                    params(Map.of("name", "Fred", "last", "Flintstone", "owe", 10, "city", "Somewhere")));
        } catch (PersismException e) {
            failed = true;
            log.info(e.getMessage(), e);
            String msg = Message.QueryPropertyNamesMissingOrNotFound.message("[firstXame, Xompany]", "");
            assertTrue("s/b (starts with) " + msg, e.getMessage().startsWith(msg));
        }
        assertTrue(failed);

        contact.setCompany("XYZ");
        session.update(contact);
        contact.setContactName("JOE");
        session.update(contact);

        log.info(contact);
    }

    public void testReuse() {
        // Because we modify SQL and Params depending on the situation test what happens if we reuse them.

        Contact contact = getContactForTest();
        session.insert(contact);

        SQL sql = where("(:firstname = @name OR :company = @name) and :lastname = @last");
        log.info("SQL before: " + sql);

        Parameters params = params(Map.of("name", "Fred", "last", "Flintstone"));
        log.info("Params before: " + params);

        List<Contact> contacts = session.query(Contact.class, sql, params);

        log.debug(contacts);

        log.info("SQL after: " + sql);
        log.info("Params after: " + params);

        contacts = session.query(Contact.class, sql, params);
        log.debug(contacts);

        // new sql same params object
        sql = where("(:firstname = @name OR :company = @name) and :lastname = @last");
        contacts = session.query(Contact.class, sql, params);
        log.info("SQL after 2: " + sql);
        log.info("Params after 2: " + params);

        log.debug(contacts);

        // new params changed SQL
        params = params(Map.of("name", "Fred", "last", "Flintstone"));

        contacts = session.query(Contact.class, sql, params);
        log.info("SQL after 3: " + sql);
        log.info("Params after 3: " + params);

        log.debug(contacts);
    }

    public void testNamedParameters() {

        // null checks for unset properties.
        Contact contact = new Contact();
        UUID rand = UUID.randomUUID();
        contact.setIdentity(rand);
        contact.setPartnerId(partnerId);
        contact.setType("X");
        contact.setFirstname("not null");
        contact.setLastname("not null");
        contact.setCompany("Y");
        contact.setContactName("X");

        session.insert(contact);
        session.fetch(contact);


        // test with primitives

        // todo HSQLDB uses "Last Name" "First Name" CANT USE property names with String class! OR CAN WE?
        // TODO IF WE DO PROPERY NAMES WE SHOULD ALLOW TABLE NAMES FROM CLASSES AS WELL.
        // FIX THESE
//        String name = session.fetch(String.class, sql("SELECT LASTNAME FROM Contacts WHERE FIRSTNAME = @first"), named(Map.of("first", "not null")));
//        assertNotNull(name);
//
//        List<String> names = session.query(String.class, sql("SELECT LASTNAME FROM Contacts WHERE FIRSTNAME = @first"), named(Map.of("first", "not null")));
//        assertTrue(names.size() > 0);

        // test with stored proc - see TestMSSQL testStoredProc
    }

    static LocalDateTime ldt = LocalDateTime.parse("1998-02-17 10:23:43.567", formatter);
    static LocalDate ld = LocalDate.parse("1997-02-17", DateTimeFormatter.ISO_DATE);
    static LocalTime lt = LocalTime.parse("10:23:43.567", DateTimeFormatter.ISO_TIME);  // earlier in the day SQLite sees INT
    static LocalTime lt2 = LocalTime.parse("22:23:41.107", DateTimeFormatter.ISO_TIME); // later on the day SQLite sees LONG FFS
    static java.util.Date udate = new java.util.Date(Timestamp.valueOf("1992-02-17 22:23:41.107").getTime());
    static java.sql.Date sdate = new java.sql.Date(udate.getTime());
    static java.sql.Timestamp ts = new Timestamp(udate.getTime());
    static java.sql.Time time = new Time(udate.getTime());

    public void testVariousTypesLikeClobAndBlob() throws Exception {

        if (connectionType == ConnectionType.Informix) {
            // https://stackoverflow.com/questions/49441015/informix-no-such-dbspace-error-when-inserting-a-record
            // todo Invalid default sbspace name (sbspace). needs to be added to docker image
            return;
        }
        // note Data is read as a CLOB
        SavedGame saveGame = new SavedGame();
        saveGame.setName("BLAH");
        saveGame.setSomeDateAndTime(new java.util.Date());
        saveGame.setData("HJ LHLH H H                     ';lk ;lk ';l k                                K HLHLHH LH LH LH LHLHLHH LH H H H LH HHLGHLJHGHGFHGFGJFDGHFDHFDGJFDKGHDGJFDD KHGD KHG DKHDTG HKG DFGHK  GLJHG LJHG LJH GLJ");
        saveGame.setGold(100.23f);
        saveGame.setSilver(200);
        saveGame.setCopper(100L);
        saveGame.setWhatTimeIsIt(new Time(System.currentTimeMillis()));
        saveGame.setSomethingBig(null);

        saveGame.setId("1");

        File file = new File(getClass().getResource("/logo1.png").toURI());
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

        List<SavedGame> savedGames = session.query(SavedGame.class, params("1"));
        log.info("ALL SAVED GAMES " + savedGames.size() + " " + savedGames.get(0).getName() + " id: " + savedGames.get(0).getId());
        saveGame = session.fetch(SavedGame.class, sql("select * from SavedGames"), none());
        assertNotNull(saveGame);
        log.info("SAVED GOLD: " + saveGame.getGold());
        log.info("SAVED SILVER: " + saveGame.getSilver());
        log.info("AFTER FETCH SIZE?" + saveGame.getSomethingBig().length);
        assertEquals("size should be the same ", size, saveGame.getSomethingBig().length);

        byte[] bytes = {};
        saveGame.setSomethingBig(bytes);
        session.update(saveGame);
        session.fetch(saveGame);

        SavedGame sg = session.fetch(SavedGame.class, where("Silver > ?"), params(199));
        log.warn(sg);
//        sg = session.fetch(SavedGame.class, proc("spSearchSilver"), params(199));
    }

    public void testAllDates() {
        SQLDateTypesTests();
        LocalDateTypesTest();
    }

    private void SQLDateTypesTests() {

        log.info("udate: " + udate + " " + udate.getTime());
        log.info("sdate: " + sdate + " " + sdate.getTime());
        log.info("ts: " + ts);
        log.info("time: " + time);

        DateTestSQLTypes testSQLTypes1 = new DateTestSQLTypes();
        testSQLTypes1.setId(1);
        testSQLTypes1.setDescription("test 1");

        testSQLTypes1.setDateOnly(sdate);
        testSQLTypes1.setTimeOnly(time);
        testSQLTypes1.setDateAndTime(ts);
        testSQLTypes1.setUtilDateAndTime(udate);
        log.info("BEFORE: " + testSQLTypes1);

        session.withTransaction(() -> {
            session.insert(testSQLTypes1);

            DateTestSQLTypes testSQLTypes2 = new DateTestSQLTypes();
            testSQLTypes2.setId(testSQLTypes1.getId());
            assertTrue(session.fetch(testSQLTypes2));

            log.info("AFTER:  " + testSQLTypes2);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss.SSS");

            assertEquals("date s/b '1992-02-17'", sdate.toString(), testSQLTypes2.getDateOnly().toString());
            assertEquals("time s/b '22:23:41'", time.toString(), testSQLTypes2.getTimeOnly().toString());
            if (connectionType == ConnectionType.MySQL) {
                // MySQL rounds off milliseconds - comes out like 1992-02-17 10:23:41.0
                String s1 = ts.toString();
                String s2 = testSQLTypes2.getDateAndTime().toString();

                assertEquals("datetime s/b '1992-02-17 22:23:41'", s1.substring(0, s1.indexOf('.')), s2.substring(0, s2.indexOf('.')));

                assertEquals("util date s/b '1992-02-17 22:23.41.000'", "1992-02-17 22:23.41.000", df.format(testSQLTypes2.getUtilDateAndTime()));

            } else {
                assertEquals("datetime s/b '1992-02-17 22:23:41.107'", ts.toString(), testSQLTypes2.getDateAndTime().toString());
                assertEquals("util date s/b '1992-02-17 22:23.41.107'", "1992-02-17 22:23.41.107", df.format(testSQLTypes2.getUtilDateAndTime()));
            }


            session.update(testSQLTypes2);

            assertTrue(session.delete(testSQLTypes1).rows() > 0);
        });
    }

    private void LocalDateTypesTest() {
        DateTestLocalTypes testLocalTypes1 = new DateTestLocalTypes();
        testLocalTypes1.setId(1);
        testLocalTypes1.setDescription("test 1");

        log.info("ldt: " + ldt);
        log.info("ld : " + ld);
        log.info("lt : " + lt);

        testLocalTypes1.setDateOnly(ld);
        testLocalTypes1.setTimeOnly(lt);
        testLocalTypes1.setDateAndTime(ldt);


        log.info("BEFORE: " + testLocalTypes1);

        session.withTransaction(() -> {
            session.insert(testLocalTypes1);

            DateTestLocalTypes testLocalTypes2 = new DateTestLocalTypes();
            testLocalTypes2.setId(testLocalTypes1.getId());
            assertTrue(session.fetch(testLocalTypes2));

            log.info("AFTER:  " + testLocalTypes2);
            String localTime = lt.format(DateTimeFormatter.ISO_TIME);
            // Remove millis since most of the DBs don't store it anyway
            if (localTime.indexOf('.') > 0) {
                localTime = localTime.substring(0, localTime.indexOf('.'));
            }
            assertEquals("date s/b '1997-02-17'", ld.format(DateTimeFormatter.ISO_DATE), testLocalTypes2.getDateOnly().format(DateTimeFormatter.ISO_DATE));
            assertEquals("time s/b '10:23:43'", localTime, testLocalTypes2.getTimeOnly().format(DateTimeFormatter.ISO_TIME));

            if (connectionType == ConnectionType.MySQL) {
                // MySQL rounds off milliseconds - 1998-02-17T10:23:43.567 comes out like 1998-02-17T10:23:43
                String s = ldt.format(DateTimeFormatter.ISO_DATE_TIME);
                assertEquals("datetime s/b '1998-02-17 10:23:43'",
                        s.substring(0, s.indexOf('.')),
                        testLocalTypes2.getDateAndTime().format(DateTimeFormatter.ISO_DATE_TIME));
            } else {
                assertEquals("datetime s/b '1998-02-17 10:23:43.567'", ldt.format(DateTimeFormatter.ISO_DATE_TIME), testLocalTypes2.getDateAndTime().format(DateTimeFormatter.ISO_DATE_TIME));
            }

            session.update(testLocalTypes2);

            DateTestLocalTypes testLocalTypes3 = new DateTestLocalTypes();
            testLocalTypes3.setId(2);
            testLocalTypes3.setDescription("time later in the day to test awfulness of SQLite");
            testLocalTypes3.setTimeOnly(lt2);

            assertEquals("s/b 1?", 1, session.insert(testLocalTypes3).rows());

            List<DateTestLocalTypes> list = session.query(DateTestLocalTypes.class, sql("select * FROM DateTestLocalTypes"));
            log.info(list);


            DateTestLocalTypes testLocalTypes4 = new DateTestLocalTypes();
            testLocalTypes4.setId(2);
            assertTrue(session.fetch(testLocalTypes4));

            localTime = lt2.format(DateTimeFormatter.ISO_TIME);
            // Remove millis since most of the DBs don't store it anyway
            if (localTime.indexOf('.') > 0) {
                localTime = localTime.substring(0, localTime.indexOf('.'));
            }
            assertEquals("time s/b " + localTime + " (FROM lt2)", localTime, testLocalTypes4.getTimeOnly().toString());


            assertTrue(session.delete(testLocalTypes1).rows() > 0);
            assertTrue(session.delete(testLocalTypes3).rows() > 0);

        });
    }

    // internal insert/update/delete/select statements pass parameters through a converter
    // try some types that might fail if not converted.
    // We might need something if we ever expose the general execute method
    public void XtestExecuteOutsideConvert() throws NoChangesDetectedForUpdateException, SQLException, InvocationTargetException, IllegalAccessException {
        Contact icontact = getContactForTest();
        session.insert(icontact);

        MetaData metaData = session.getMetaData();

        Contact contact = getContactForTest();
        String updateSQL = metaData.getUpdateStatement(contact, con);
        log.info("UPDATE SQL: " + updateSQL);

        List<String> primaryKeys = metaData.getPrimaryKeys(contact.getClass(), con);

        List<Object> params = new ArrayList<>(primaryKeys.size());
        Map<String, PropertyInfo> changedProperties = metaData.getChangedProperties(contact, con);
        Map<String, PropertyInfo> allProperties = metaData.getTableColumnsPropertyInfo(contact.getClass(), con);

        for (String column : changedProperties.keySet()) {

            if (!primaryKeys.contains(column)) {
                Object value = allProperties.get(column).getValue(contact);

                params.add(value);
            }
        }

        for (String column : primaryKeys) {
            params.add(allProperties.get(column).getValue(contact));
        }

        log.info(updateSQL);
        log.info(params);
        log.info("param size? " + params.size());
        log.info("param count? " + countOccurrences("?", updateSQL));
        log.info("param 18? " + params.get(17));
        try (PreparedStatement st = con.prepareStatement(updateSQL)) {

            // METHOD ONE: Raw.
            // Fails every DB
//            for (int j = 0; j < params.size(); j++) {
//                st.setObject(j+1, params.get(j));
//            }

            // METHOD TWO: call setParameters which does some checking
            // Fails the local DBs H2, HSQLDB, Derby and also Firebird
            session.helper.setParameters(st, params.toArray());

            // METHOD THREE: What I do normally which is to pass through convert and then use setParameters
            // I guess if we ever support a general execute method we could either leave this up to the user
            // or have some kind of in-between call for the method.
            st.executeUpdate();
        }

    }

    public void testIsNamedBooleanProperties() throws SQLException {
        Order order = DAOFactory.newOrder(con);
        order.setName("test 1");
        order.setCancelled(true);
        order.setCollect(true);
        order.setCancelled(true);
        order.setPrepaid(true);
        order.setPaid(true);
        session.insert(order);

        assertTrue("order # > 0", order.getId() > 0);

        Order order2 = DAOFactory.newOrder(con);
        order2.setId(order.getId());
        assertTrue("should be found ", session.fetch(order2));

        assertTrue("paid", order2.isPaid());
        assertTrue("cancelled", order2.isCancelled());
        assertTrue("prepaid", order2.isPrepaid());
        assertTrue("collect", order2.isCollect());
    }

    public void testInvoice() {

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        //customer.setCountry("US");
        customer.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Region.East);
        //customer.setStatus('1');
        session.insert(customer);

        session.fetch(customer);


        Customer customer2 = new Customer();
        customer2.setCompanyName("TEST2");
        customer2.setCustomerId("MOO2");
        customer2.setAddress("123 sesame street 2");
        customer2.setCity("city");
        customer2.setContactName("fred flintstone");
        customer2.setContactTitle("Lord");
        customer2.setCountry("CA");
        // customer2.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));
        customer2.setFax("123-456-7890");
        customer2.setPhone("456-678-1234");
        customer2.setPostalCode("54321");
        customer2.setRegion(Region.East);
        customer2.setStatus('1');
        session.insert(customer2);

        Customer customer3 = new Customer();
        customer3.setCompanyName("TEST2");
        customer3.setCustomerId("MOO3");
        customer3.setAddress("123 sesame street 3");
        customer3.setCity("city");
        customer3.setContactName("fred flintstone");
        customer3.setContactTitle("Lord");
        // customer3.setCountry("CA");
        // customer3.setDateRegistered(new java.sql.Timestamp(System.currentTimeMillis()));
        customer3.setFax("123-456-7890");
        customer3.setPhone("456-678-1234");
        customer3.setPostalCode("54321");
        customer3.setRegion(Region.East);
        customer3.setStatus('1');
        session.insert(customer3);


        assertEquals("country s/b US", "US", customer.getCountry());

        Invoice invoice = new Invoice();
        invoice.setCustomerId("123");
        invoice.setPrice(10.5f);
        invoice.setQuantity(10);
        invoice.setPaid(true);
        invoice.setActualPrice(new BigDecimal("10.23"));

        assertEquals("s/b 1", 1, session.insert(invoice).rows());


        assertTrue("Invoice ID > 0", invoice.getInvoiceId() > 0);
        assertNotNull("Created s/b not null", invoice.getCreated()); // note no setter

// todo add test select columns in reverse order or similar

        List<Invoice> invoices = session.query(Invoice.class, where("CUSTOMER_ID=? ORDER BY CUSTOMER_ID"), params("123"));
        log.info(invoices);
        assertEquals("invoices s/b 1", 1, invoices.size());

        invoice = invoices.get(0);

        log.info(invoice);

        assertEquals("customer s/b 123", "123", invoice.getCustomerId());
        assertEquals("invoice # s/b 1", 1, invoice.getInvoiceId().intValue());
        assertEquals("price s/b 10.5", 10.5f, invoice.getPrice());
        assertEquals("qty s/b 10", 10, invoice.getQuantity());
        assertTrue("paid s/b true", invoice.isPaid());

        NumberFormat nf = NumberFormat.getInstance();

        assertEquals("totals/b 105.00", nf.format(105.0f), invoice.getTotal().toString());

        boolean fail = false;

        try {
            // invoice fail
            session.query(InvoiceFail.class, where("CUSTOMER_ID=? ORDER BY CUSTOMER_ID"), params("123"));
        } catch (PersismException e) {
            fail = true;
            String msg = Message.PropertyCountMismatchForJoin.message(InvoiceFail.class, "invoiceId, price", "invoiceId");
            assertEquals("msg s/b ' " + msg + "' ", msg, e.getMessage());
        }
        assertTrue(fail);
    }

    // RecordTest1 is invalid, so it should fail on query and fetch
    public void testRecord1() {
        UUID id = UUID.randomUUID();
        RecordTest1 rt1 = new RecordTest1(id, "test 1", 10, 4.23f, 0.0d);

        session.insert(rt1);

        Object paramValue = switch (session.getMetaData().getConnectionType()) { // todo verify
            // don't really know yet.
            case MSSQL, JTDS, UCanAccess, Informix, SQLite, Firebird, PostgreSQL -> id;
            case MySQL, Oracle, Other, Derby, HSQLDB, H2 -> Converter.asBytes(id);
        };

        // Any fetch or query should fail - see RecordTest1 has a bad constructor
        boolean fail = false;
        try {
            log.warn("paramValue: " + paramValue + " " + paramValue.getClass());
            //session.fetch(RecordTest1.class, params((Object) Converter.asBytes(id)));
            session.fetch(RecordTest1.class, params(paramValue));
        } catch (PersismException e) {
            fail = true;
            log.warn(e.getMessage(), e);
            log.warn(Message.ReadRecordCouldNotInstantiate.message(RecordTest1.class, "..."));
            assertTrue("msg should start with 'readRecord: Could not instantiate the constructor for: class net.sf.persism.dao.records.RecordTest1'",
                    e.getMessage().startsWith("readRecord: Could not instantiate the constructor for: class net.sf.persism.dao.records.RecordTest1"));
        }
        assertTrue(fail);

        fail = false;
        try {
            session.fetch(rt1);
        } catch (PersismException e) {
            fail = true;
            assertEquals("s/b 'class net.sf.persism.dao.records.RecordTest1: FETCH operation not supported for record types'",
                    "class net.sf.persism.dao.records.RecordTest1: FETCH operation not supported for record types",
                    e.getMessage());
        }
        assertTrue(fail);

    }

    public void testRecord2() {
        RecordTest2 rt2 = new RecordTest2(0, "test 1", 10, 3.99, LocalDateTime.now());
        log.info(rt2);
        Result<RecordTest2> result = session.insert(rt2);
        log.info("before: " + rt2);
        log.info("after : " + result.dataObject());

        log.info(rt2.total());

        log.info("testRecord2 ALL? \n" + session.query(RecordTest2.class));

        RecordTest2 rt22 = session.fetch(RecordTest2.class, sql("select CrEATED_ON, PRiCE, QtY, DESCrIPTION, iD FROM RecordTest2 where ID = ?"), params(1));
        log.info(rt22);
        assertNotNull(rt22);
        log.info(rt22.total());

        RecordTest2 rt23 = new RecordTest2(2, "test 2", 1, 0.05d);
        session.insert(rt23);
        log.info(rt23);

        // THis cannot be fetched without the Created_ON column
        boolean fail = false;
        try {
            rt23 = session.fetch(RecordTest2.class, sql("select PRiCE, QtY, DESCrIPTION, iD FROM RecordTest2 where ID = ?"), params(2));

            log.info(rt23);

        } catch (PersismException e) {
            log.error(e.getMessage(), e);
            fail = true;
            // todo this should be the common object message instead defined by Messages.ObjectNotProperlyInitializedByQuery
            assertEquals("s/b 'readrecord: could not find column in the sql query for class: class net.sf.persism.dao.records.recordtest2. missing column: created_on'".toLowerCase(),
                    "readrecord: could not find column in the sql query for class: class net.sf.persism.dao.records.recordtest2. missing column: created_on".toLowerCase(),
                    e.getMessage().toLowerCase());
        }

        assertTrue(fail);
    }

    // TODO LATER
    public void XtestGetMultipleResultSets() throws Exception {
        String sql = """
                SELECT * FROM CUSTOMERS;

                SELECT * FROM INVOICES;

                SELECT * FROM CONTACTS;
                                
                """;

        try (Statement st = con.createStatement()) {
            int n = 0;
            st.execute(sql);
            ResultSet rs = st.getResultSet();
            System.out.println(rs.getMetaData().getColumnLabel(2));
            n++;
            while (st.getMoreResults(Statement.KEEP_CURRENT_RESULT)) {
                rs = st.getResultSet();
                System.out.println(rs.getMetaData().getColumnLabel(2));
                System.out.println(++n);
            }
            System.out.println(n);
        }

    }

    public void testDelete() {
        log.info(session.metaData.getDeleteStatement(Customer.class, con));
        log.info(session.metaData.getDefaultDeleteStatement(Customer.class, con));

        Customer customer = new Customer();
        customer.setCustomerId("DELETEME");
        customer.setRegion(Region.North);
        customer.setCompanyName("test 1243");
        session.insert(customer);
        int result = session.delete(Customer.class, where(":region = ?"), params(Region.North));
        log.info(result);
        assertEquals("s/b 1", 1, result);

        result = session.delete(Customer.class, params("1", "3", "hello"));
        assertEquals("s/b 0", 0, result);


        messageTester(DeleteExpectsInstanceOfDataObjectNotAClass.message(Customer.class.getName()), () -> session.delete(Customer.class));
        messageTester(CannotDeleteWithNoPrimaryKeys.message(), () -> session.delete(Customer.class, params()));
        messageTester(DeleteCanOnlyUseWhereClause.message(), () -> session.delete(Customer.class, sql("should fail")));
    }

    public void testRecords() {
        RecordTest2 rt2 = new RecordTest2(0, "desc2", 100, 25.434f);
        log.info(rt2);

        Result<RecordTest2> result = session.insert(rt2);
        assertEquals("id should still be 0", 0, rt2.id());
        assertNull("should have null createdOn", rt2.createdOn());
        log.info(rt2);

        assertEquals("rows s/b 1", 1, result.rows());
        assertEquals("Object ID s/b 1", 1, result.dataObject().id());
        assertNotNull("should have createdOn ", result.dataObject().createdOn());
        log.info("after: " + result.dataObject());

        messageTester(OperationNotSupportedForRecord.message(RecordTest2.class, "FETCH"), () -> session.fetch(rt2));
    }

    public void testFetchOnViewShouldFail() {

        messageTester(Message.OperationNotSupportedForView.message(CustomerInvoice.class, "FETCH"), () -> {
            CustomerInvoice ci = new CustomerInvoice();
            session.fetch(ci); // fail can't fetch view
        });

        // this should work
        session.fetch(CustomerInvoice.class, where("1=1"));
    }

    public void testQueryOnViewWithPrimaryKeysShouldFail() {
        String message = Message.OperationNotSupportedForView.message(CustomerInvoice.class, "QUERY w/o specifying the SQL with @View since we don't have Primary Keys");
        messageTester(message, () -> session.query(CustomerInvoice.class, params(1, 2, 3)));
    }

    public void testFetchOnNonTableClass() {
        CustomerInvoice customerInvoice = new CustomerInvoice();
        messageTester(OperationNotSupportedForView.message(CustomerInvoice.class, "FETCH"), () -> session.fetch(customerInvoice));
    }

    public void testFetchWithPrimitiveShouldFail() {
        messageTester(OperationNotSupportedForJavaType.message(String.class, "FETCH"), () -> session.fetch(""));
    }

    public void testDeleteWithPrimaryKeysNoParamsShouldFail() {
        messageTester(CannotDeleteWithNoPrimaryKeys.message(), () -> session.delete(Customer.class, none()));
    }


    public void testCheckIfOkForWriteOperationForInvalidCases() {
        messageTester(OperationNotSupportedForView.message(CustomerInvoice.class, "INSERT"), () -> session.insert(new CustomerInvoice()));
        messageTester(OperationNotSupportedForNotTableQuery.message(CustomerOrder.class, "INSERT"), () -> session.insert(new CustomerOrder()));
        messageTester(OperationNotSupportedForJavaType.message(java.util.Date.class, "INSERT"), () -> session.insert(new java.util.Date()));
    }

    public void testJoinToNullCollection() {
        queryDataSetup();

        messageTester(CannotNotJoinToNullProperty.message("invoices"), () -> session.query(CustomerFail3.class, none()));
        messageTester(CannotNotJoinToNullProperty.message("invoices"), () -> session.fetch(CustomerFail3.class, params("123")));
    }

    // todo metadata call getDefaultSelectStatement on view?
    // @OrderWith()
    public void testGetDbMetaData() throws SQLException {
        if (true) {
            return;
        }
        DatabaseMetaData dmd = con.getMetaData();
        System.out.println("GetDbMetaData for " + dmd.getDatabaseProductName());

        System.out.println("tables and views?");

        String[] tableTypes = {"TABLE", "VIEW"};

        ResultSetMetaData rsmd;
        ResultSet rs;
        // get attributes
        //rs = dmd.getAttributes("", "", "", "");
        //List<String> tables = new ArrayList<>(32);
        rs = dmd.getTables(null, session.getMetaData().getConnectionType().getSchemaPattern(), null, tableTypes);
        rsmd = rs.getMetaData();
        while (rs.next()) {
//            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//                System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
//            }
            //  tables.add(rs.getString("TABLE_NAME"));
            System.out.println(rs.getString("TABLE_NAME") + " " + rs.getString("TABLE_TYPE"));
        }
        System.out.println("----------");
        if (true) {
            return;
        }

//        System.out.println("PROCS");
//        System.out.println("-----");
//        ResultSet result = dmd.getProcedures(null, "%", "%");
//        for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
//            System.out.println(i + " - " + result.getMetaData().getColumnLabel(i));
//        }
//
//        System.out.println("Catalog\tSchema\tName");
//        while (result.next()) {
//            System.out.println(result.getString("PROCEDURE_CAT") +
//                    " - " + result.getString("PROCEDURE_SCHEM") +
//                    " - " + result.getString("PROCEDURE_NAME"));
//        }

//        String[] tableTypes = {"TABLE"};
//
//        ResultSetMetaData rsmd;
//        ResultSet rs;
//        // get attributes
//        //rs = dmd.getAttributes("", "", "", "");
//        List<String> tables = new ArrayList<>(32);
//        rs = dmd.getTables(null, session.getMetaData().getConnectionType().getSchemaPattern(), null, tableTypes);
//        rsmd = rs.getMetaData();
//        while (rs.next()) {
//            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//                System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
//            }
//            tables.add(rs.getString("TABLE_NAME"));
//            System.out.println("----------");
//        }
//
//        for (String table : tables) {
//            System.out.println("Table " + table + " COLUMN INFO");
//            rs = dmd.getColumns(null, session.getMetaData().getConnectionType().getSchemaPattern(), table, null);
//            rsmd = rs.getMetaData();
//            while (rs.next()) {
//                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//                    System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
//                }
//                System.out.println("----------");
//            }
//
//        }

        System.out.println("VIEWS");
        System.out.println("-----");

        String[] viewType = {"VIEW"};

        // get attributes
        List<String> views = new ArrayList<>(32);
        rs = dmd.getTables(null, session.getMetaData().getConnectionType().getSchemaPattern(), null, viewType);
        rsmd = rs.getMetaData();
        while (rs.next()) {
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
            }
            views.add(rs.getString("TABLE_NAME"));
            System.out.println("----------");
        }

        for (String view : views) {
            System.out.println("VIEW " + view + " COLUMN INFO");
            rs = dmd.getColumns(null, session.getMetaData().getConnectionType().getSchemaPattern(), view, null);
            rsmd = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
                }
                System.out.println("----------");
            }

        }

    }

    static void executeCommands(List<String> commands, Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            for (String command : commands) {
                log.info(command);
                st.execute(command);
            }
        }
    }

    // use if you want to run commands one at a time for debugging or testing
    static void executeCommand(String command, Connection con) throws SQLException {
        //System.out.println(command);
        log.info(command);
        try (Statement st = con.createStatement()) {
            st.execute(command);
        }
    }

    private static int countOccurrences(String findStr, String instring) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = instring.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

    // https://stackoverflow.com/questions/67126109/is-there-a-way-to-recognise-a-java-16-records-canonical-constructor-via-reflect
    // Can't be used with Java 8
    static <T> Constructor<T> getCanonicalConstructor(Class<T> recordClass)
            throws NoSuchMethodException, SecurityException {

        var components = recordClass.getRecordComponents();
        if (components == null) {
            log.warn("why are you calling this on a non-record?", new Throwable(""));
            return null;
        }
        Class<?>[] componentTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        return recordClass.getDeclaredConstructor(componentTypes);
    }

    // todo clean up for findConstructor call in Reader.
    final void TODOtestConstructors() {
        List<Constructor<?>> constructors = new ArrayList<>();
        Constructor<?> selectedConstructor = null;
        if (log.isDebugEnabled()) {
            int x = 0;
            for (Constructor<?> constructor : constructors) {
                log.debug("CON " + (x++) + " " + constructor.equals(selectedConstructor) + " -> " + debugConstructor(constructor));
            }
            log.debug(Arrays.asList(constructors));
            log.debug("INDEX: " + Arrays.asList(constructors).indexOf(selectedConstructor));
            log.debug("findConstructor selected: %s", debugConstructor(selectedConstructor));
        }

    }

    final String debugConstructor(Constructor<?> constructor) {

        if (constructor == null) {
            return null;
        }
        ConstructorTag annotation = constructor.getAnnotation(ConstructorTag.class);
        String tag = "";

        if (annotation != null) {
            tag = annotation.value();
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Parameter p : constructor.getParameters()) {
            sb.append(sep).append(p.getName());
            sep = ",";
        }

        return tag + " " + constructor + " names: " + sb;
    }


}
