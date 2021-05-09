package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.System.out;

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

    ConnectionTypes connectionType;

    static boolean mssqlmode = true;

    static String UUID1 = "d316ad81-946d-416b-98e3-3f3b03aa73db";
    static String UUID2 = "a0d00c5a-3de6-4ae8-ba11-e3e02c2b3a83";

    protected abstract void createTables() throws SQLException;

    @Override
    protected void setUp() throws Exception {
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

    public void testDates() {
        List<Customer> list = session.query(Customer.class, "select * from Customers");
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
        customer.setRegion(Regions.East);
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

        if (connectionType == ConnectionTypes.SQLite || connectionType == ConnectionTypes.Oracle) {
            // SQLite does not support DATE on it's own
            // Oracle DATE also includes time so JDBC driver reports it as timestamp
        } else {
            // Checking to see if time part is removed when using 'DATE' column to LocalDateTime
            assertEquals("date of last order s/b", dateOfLastOrder, dtf.format(customer2.getDateOfLastOrder()));
        }

        assertEquals("date registration s/b", dateRegistered, df.format(customer2.getDateRegistered()));
    }

    public void testStoredProcs() {

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
            assertEquals("Customer 1 date registered should be null", null, customer1.getDateRegistered());


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
        customer.setRegion(Regions.East);
        customer.setStatus('1');

        session.delete(customer); // i case it already exists.
        session.insert(customer);

        customer.setRegion(Regions.North);
        session.update(customer);

        boolean failOnMissingProperties = false;

        try {
            session.query(Customer.class, "SELECT Country, PHONE from Customers");
        } catch (Exception e) {
            log.info(e.getMessage());
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        List<Customer> list = session.query(Customer.class, "SELECT company_NAME, Date_Of_Last_ORDER, contact_title, pHone, rEGion, postal_CODE, FAX, DATE_Registered, ADDress, CUStomer_id, Contact_name, country, city, STATUS, TestLocalDate, TestLocalDateTime from Customers");

        log.info(list);
        assertEquals("list should be 1", 1, list.size());

        Customer c2 = list.get(0);
        assertEquals("region s/b north ", Regions.North, c2.getRegion());
    }

    public void testQueryResult() throws Exception {

        setupDataForQuery();

        // REMOVE DATE_PAID ALIAS
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.Created AS DateCreated, o.PAID ");
        sb.append(" FROM Orders o");
        sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

        String sql = sb.toString();
        log.info(sql);

        List<CustomerOrder> results = session.query(CustomerOrder.class, sql);
        log.info(results);
        assertEquals("size should be 4", 4, results.size());

        // ORDER 1 s/b paid = true others paid = false
        for (CustomerOrder customerOrder : results) {
            log.info("date created? " + customerOrder.getDateCreated());
            log.info("date paid? " + customerOrder.getDatePaid());
            if ("ORDER 1".equals(customerOrder.getDescription())) {
                assertTrue("order 1 s/b paid", customerOrder.isPaid());
            } else {
                assertFalse("order OTHER s/b NOT paid", customerOrder.isPaid());
            }
        }
    }

    private void setupDataForQuery() throws SQLException {
        Customer c1 = new Customer();
        c1.setCustomerId("123");
        c1.setCompanyName("ABC INC");
        c1.setStatus('x');
        session.insert(c1);

        Customer c2 = new Customer();
        c2.setCustomerId("456");
        c2.setCompanyName("XYZ INC");
        c2.setStatus('2');
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

        List<Order> orders = session.query(Order.class, "select * from Orders");
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
        customer.setRegion(Regions.East);
        customer.setStatus('2');


        session.delete(customer); // in case it already exists.
        session.insert(customer);

        List<String> list = session.query(String.class, "SELECT Country from Customers");


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

        String countryString = session.fetch(String.class, "SELECT Country from Customers");
        assertEquals("String should be US", "US", countryString);

        countryString = "NOT US";
        countryString = session.fetch(String.class, "SELECT Country from Customers");
        assertEquals("String should be US", "US", countryString);

        List<Date> dates = session.query(Date.class, "select Date_Registered from Customers ");
        log.info(dates);

        Date dt = session.fetch(Date.class, "select Date_Registered from Customers ");
        log.info(dt);

        // Fails because there is no way to instantiate java.sql.Date - no default constructor.
        List<java.sql.Date> sdates = session.query(java.sql.Date.class, "select Date_Registered from Customers ");
        log.info(sdates);

        java.sql.Date sdt = session.fetch(java.sql.Date.class, "select Date_Registered from Customers ");
        log.info(sdt);

        // this should fail. We can't do simple read on a primitive
        boolean failed = false;
        try {
            session.fetch(countryString);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'Cannot read a primitive type object with this method.'", "Cannot read a primitive type object with this method.", e.getMessage());
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
        contact.setTestInstant(ldt4.toInstant(ZoneOffset.UTC));
        contact.setTestInstant2(ldt4.toInstant(ZoneOffset.UTC));
        contact.setSomeDate(date);
        return contact;
    }

    public void testContactTable() throws SQLException {

        Contact contact = getContactForTest();

        log.info("Local Date: " + ldt4 + " INSTANT: " + contact.getTestInstant());
        log.info("Local Date: " + LocalDateTime.now() + " INSTANT: " + Instant.now());

        session.insert(contact);
        contact.setNotes(null);
        session.update(contact);

        Contact contact2 = new Contact();
        contact2.setIdentity(identity);
        assertTrue(session.fetch(contact2));
        assertNotNull(contact2.getPartnerId());
        assertEquals(contact2.getIdentity(), identity);
        assertEquals(contact2.getPartnerId(), partnerId);

        contact.setDivision("Y");
        assertEquals("1 update?", 1, session.update(contact));

        contact.setDivision("Y");
        assertEquals("0 update?", 0, session.update(contact));

        List<Contact> contacts = session.query(Contact.class, "SELECT * FROM Contacts");
        assertEquals("should have 1? ", 1, contacts.size());

        Contact contact1 = contacts.get(0);
        log.info("CONTACT: " + contact1);

        assertEquals("1?", 1, session.delete(contact));

        assertEquals("UDDI should be the same ", UUID1, contact1.getIdentity().toString());
        assertEquals("UDDI should be the same ", UUID2, contact1.getPartnerId().toString());

        assertEquals("Date Added sql.Date s/b '1998-02-17'", "1998-02-17", "" + contact1.getDateAdded());

        // MySQL fails with minor accuracy
        // Expected :1997-02-17 10:23:43.123
        // Actual   :1997-02-17 10:23:43.0
        // https://dev.mysql.com/doc/refman/5.7/en/date-and-time-types.html
        // Has the accuracy in v8 so once we update the DB and driver we should retest
        if (connectionType == ConnectionTypes.MySQL) {
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
                contactForTest.setAddress1("123 Somewhere");
                contactForTest.setContactName("HELLO?!");
                contactForTest.setNotes("notes?");
                session.update(contactForTest);
                session.fetch(contactForTest);

                log.info("contact after insert/update before commit/rollback: " + contactForTest);

                // NOW FAIL the transaction to see that the new contact was not committed
                session.query(Object.class, "select * FROM TABLE THAT DOESN'T EXIST!!!!");
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

    }

    static LocalDateTime ldt = LocalDateTime.parse("1998-02-17 10:23:43.567", formatter);
    static LocalDate ld = LocalDate.parse("1997-02-17", DateTimeFormatter.ISO_DATE);
    static LocalTime lt = LocalTime.parse("10:23:43.567", DateTimeFormatter.ISO_TIME);  // earlier in the day SQLite sees INT
    static LocalTime lt2 = LocalTime.parse("22:23:41.107", DateTimeFormatter.ISO_TIME); // later on the day SQLite sees LONG FFS
    static java.util.Date udate = new java.util.Date(Timestamp.valueOf("1992-02-17 22:23:41.107").getTime());
    static java.sql.Date sdate = new java.sql.Date(udate.getTime());
    static java.sql.Timestamp ts = new Timestamp(udate.getTime());
    static java.sql.Time time = new Time(udate.getTime());

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

        session.insert(testSQLTypes1);

        DateTestSQLTypes testSQLTypes2 = new DateTestSQLTypes();
        testSQLTypes2.setId(testSQLTypes1.getId());
        assertTrue(session.fetch(testSQLTypes2));

        log.info("AFTER:  " + testSQLTypes2);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss.SSS");

        assertEquals("date s/b '1992-02-17'", sdate.toString(), testSQLTypes2.getDateOnly().toString());
        assertEquals("time s/b '22:23:41'", time.toString(), testSQLTypes2.getTimeOnly().toString());
        if (connectionType == ConnectionTypes.MySQL) {
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

        List<DateTestSQLTypes> list = session.query(DateTestSQLTypes.class, "select * FROM DateTestSQLTypes");
        log.info(list);

        assertTrue(session.delete(testSQLTypes1) > 0);

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

        if (connectionType == ConnectionTypes.MySQL) {
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

        List<DateTestLocalTypes> list = session.query(DateTestLocalTypes.class, "select * FROM DateTestLocalTypes");
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


        assertTrue(session.delete(testLocalTypes1) > 0);
        assertTrue(session.delete(testLocalTypes3) > 0);
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
                Object value = allProperties.get(column).getter.invoke(contact);

                params.add(value);
            }
        }

        for (String column : primaryKeys) {
            params.add(allProperties.get(column).getter.invoke(contact));
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
            session.setParameters(st, params.toArray());

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
        customer.setRegion(Regions.East);
        //customer.setStatus('1');
        session.insert(customer);

        session.fetch(customer); // DOESNT FAIl?

        assertEquals("country s/b US", "US", customer.getCountry());

        Invoice invoice = new Invoice();
        invoice.setCustomerId("MOO");
        invoice.setPrice(10.5f);
        invoice.setQuantity(10);
        invoice.setTotal(new BigDecimal(invoice.getPrice() * invoice.getQuantity()));
        invoice.setPaid(true);
        invoice.setActualPrice(new BigDecimal("10.23"));

        session.insert(invoice);

        assertTrue("Invoice ID > 0", invoice.getInvoiceId() > 0);
        assertNotNull("Created s/b not null", invoice.getCreated()); // note no setter
        assertNotNull("what about junk1? ", invoice.getJunk1());

        List<Invoice> invoices = session.query(Invoice.class, "select * from Invoices where CUSTOMER_ID=?", "MOO");
        log.info(invoices);
        assertEquals("invoices s/b 1", 1, invoices.size());

        invoice = invoices.get(0);

        log.info(invoice);

        assertEquals("customer s/b MOO", "MOO", invoice.getCustomerId());
        assertEquals("invoice # s/b 1", 1, invoice.getInvoiceId().intValue());
        assertEquals("price s/b 10.5", 10.5f, invoice.getPrice());
        assertEquals("qty s/b 10", 10, invoice.getQuantity());

    }


    public void testGetDbMetaData() throws SQLException {
//        if (true) {
//            return;
//        }
        DatabaseMetaData dmd = con.getMetaData();
        log.info("GetDbMetaData for " + dmd.getDatabaseProductName());

        log.info("PROCEDURES?");
        ResultSet result = dmd.getProcedures(null, "%", "%");
        for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
            out.println(i + " - " + result.getMetaData().getColumnLabel(i));
        }


        out.println("Catalog\tSchema\tName");
        while (result.next()) {
            out.println("cat: " + result.getString("PROCEDURE_CAT") +
                    " schem: " + result.getString("PROCEDURE_SCHEM") +
                    " name: " + result.getString("PROCEDURE_NAME"));
        }

        log.info("VIEWS!");
        String[] tableTypes = {"VIEW"};

        ResultSetMetaData rsmd;
        ResultSet rs;
        // get attributes
        //rs = dmd.getAttributes("", "", "", "");
        List<String> views = new ArrayList<>(32);
        rs = dmd.getTables(null, session.getMetaData().getConnectionType().getSchemaPattern(), null, tableTypes);
        rsmd = rs.getMetaData();
        while (rs.next()) {
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                log.info(rsmd.getColumnName(i) + " = " + rs.getObject(i));
            }
            views.add(rs.getString("TABLE_NAME"));
            log.info("----------");
        }
        log.info("*************************** VIEWS!");

        tableTypes[0] = "TABLE";

        // get attributes
        //rs = dmd.getAttributes("", "", "", "");
        List<String> tables = new ArrayList<>(32);
        rs = dmd.getTables(null, session.getMetaData().getConnectionType().getSchemaPattern(), null, tableTypes);
        rsmd = rs.getMetaData();
        while (rs.next()) {
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                log.info(rsmd.getColumnName(i) + " = " + rs.getObject(i));
            }
            tables.add(rs.getString("TABLE_NAME"));
            log.info("----------");
        }

        for (String table : tables) {
            log.info("Table " + table + " COLUMN INFO");
            rs = dmd.getColumns(null, session.getMetaData().getConnectionType().getSchemaPattern(), table, null);
            rsmd = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    log.info(rsmd.getColumnName(i) + " = " + rs.getObject(i));
                }
                log.info("----------");
            }

        }

//        rs = dmd.getColumns("", session.getMetaData().connectionType.getSchemaPattern(), "", "");
//        rsmd = rs.getMetaData();
//        for (int i = 1; i<= rsmd.getColumnCount(); i++) {
//            log.info(rsmd.getColumnName(i) + " = " + rs.getObject(i));
//        }

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
        try (Statement st = con.createStatement()) {
            log.info(command);
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

}
