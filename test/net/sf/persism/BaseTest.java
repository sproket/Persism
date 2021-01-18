package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.*;

import java.sql.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    static boolean mssqlmode = true;

    @Override
    protected void setUp() throws Exception {

        // todo review subclass setup methods. We probably don't need to do all that work between each test. Add a constructor for it instead and see how often they run then...

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
        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setAddress("123 Sesame Street");
        customer.setCity("MTL");
        customer.setCompanyName("ABC Inc");
        customer.setContactName("Fred");
        customer.setContactTitle("LORD");
        customer.setCountry("USA");

        customer.setFax("fax");
        customer.setPhone("phone");
        customer.setPostalCode("12345");
        customer.setRegion(Regions.East);
        customer.setStatus('2');
        /*

         */

        String dateOfLastOrder = "20120528192835";
        String dateRegistered = "20110612185225";
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

        customer.setDateOfLastOrder(new java.sql.Date(UtilsForTests.getCalendarFromAnsiDateString(dateOfLastOrder).getTime().getTime()));
        log.info(customer.getDateOfLastOrder());
        customer.setDateRegistered(new java.sql.Date(UtilsForTests.getCalendarFromAnsiDateString(dateRegistered).getTimeInMillis()));
        log.info(customer.getDateRegistered());

        assertEquals("date of last order s/b", dateOfLastOrder, df.format(customer.getDateOfLastOrder()));
        assertEquals("date registration s/b", dateRegistered, df.format(customer.getDateRegistered()));

        session.insert(customer);

        Customer customer2 = new Customer();
        customer2.setCustomerId(customer.getCustomerId());

        session.fetch(customer2);

        assertEquals("date of last order s/b", dateOfLastOrder, df.format(customer2.getDateOfLastOrder()));
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
            customer1.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));
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

            // todo readObject should fail if ID not found??? this test is useless, read returns boolean
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
        customer.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Regions.East);
        customer.setStatus('1');
        session.delete(customer); // i case it already exists.
        session.insert(customer);

        customer.setRegion(Regions.North);
        session.update(customer);

//
        boolean failOnMissingProperties = false;

        try {
            session.query(Customer.class, "SELECT Country from CUSTOMERS");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        List<Customer> list = session.query(Customer.class, "SELECT company_NAME, Date_Of_Last_ORDER, contact_title, pHone, rEGion, postal_CODE, FAX, DATE_Registered, ADDress, CUStomer_id, Contact_name, country, city, STATUS from CUSTOMERS");

        // TODO TEST java.lang.IllegalArgumentException: argument type mismatch. Column: rEGion Type of property: class net.sf.persism.dao.Regions - Type read: class java.lang.String VALUE: BC
        // Add a value outside the enum to reproduce this error. IT IS A GOOD ERROR - we WANT TO THROW THIS so a user knows they have a value outside the ENUM

        log.info(list);
        assertEquals("list should be 1", 1, list.size());

        Customer c2 = list.get(0);
        assertEquals("region s/b north ", Regions.North, c2.getRegion());
    }

    public void testQueryResult() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Created AS DateCreated, o.PAID ");
            sb.append(" FROM ORDERS o");
            sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

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
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            order.setPaid(true);
            session.insert(order);

            assertTrue("order # > 0", order.getId() > 0);

            List<Order> orders = session.query(Order.class, "select * from orders");
            assertEquals("should have 1 order", 1, orders.size());
            assertTrue("order id s/b > 0", orders.get(0).getId() > 0);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("ORDER 2");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            session.insert(order);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("456");
            order.setName("ORDER 3");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            session.insert(order);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("456");
            order.setName("ORDER 4");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            session.insert(order);

            String sql = sb.toString();
            log.info(sql);

            List<CustomerOrder> results = session.query(CustomerOrder.class, sql);
            log.info(results);
            assertEquals("size should be 4", 4, results.size());

            // ORDER 1 s/b paid = true others paid = false
            for (CustomerOrder customerOrder : results) {
                if ("ORDER 1".equals(customerOrder.getDescription())) {
                    assertTrue("order 1 s/b paid", customerOrder.isPaid());
                } else {
                    assertFalse("order OTHER s/b NOT paid", customerOrder.isPaid());
                }
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }


    public void testReadPrimitive() {

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
        customer.setStatus('2');

        session.delete(customer); // i case it already exists.
        session.insert(customer);

        List<String> list = session.query(String.class, "SELECT Country from CUSTOMERS");

        assertEquals("list should have 1", 1, list.size());
        assertEquals("String should be US", "US", list.get(0));

        String country = session.fetch(String.class, "SELECT Country from CUSTOMERS");
        assertEquals("String should be US", "US", country);

        country = "NOT US";

        country = session.fetch(String.class, "SELECT Country from CUSTOMERS");

        assertEquals("String should be US", "US", country);

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
            session.fetch(country);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'Cannot read a primitive type object with this method.'", "Cannot read a primitive type object with this method.", e.getMessage());
        }
        assertTrue("should have thrown the exception", failed);

    }

    public void XtestGetDbMetaData() throws SQLException {
        DatabaseMetaData dmd = con.getMetaData();
        log.info("GetDbMetaData for " + dmd.getDatabaseProductName());

        String[] tableTypes = {"TABLE"};

        ResultSetMetaData rsmd;
        ResultSet rs;
        // get attributes
        //rs = dmd.getAttributes("", "", "", "");
        List<String> tables = new ArrayList<>(32);
        rs = dmd.getTables(null, session.getMetaData().connectionType.getSchemaPattern(), null, tableTypes);
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
            rs = dmd.getColumns(null, session.getMetaData().connectionType.getSchemaPattern(), table, null);
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

    protected abstract void createTables() throws SQLException;


}
