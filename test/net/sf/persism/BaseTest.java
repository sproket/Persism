package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Comments for BaseTest go here.
 *
 * @author Dan Howard
 * @since 10/8/11 6:24 PM
 */
public abstract class BaseTest extends TestCase {

    private static final Log log = Log.getLogger(BaseTest.class);

    Connection con;
    Query query;
    Command command;


    @Override
    protected void setUp() throws Exception {

        // todo review subclass setup methods. We probably don't need to do all that work between each test. Add a constructor for it instead and see how often they run then...

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (con != null) {
            MetaData.removeInstance(con);
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

        command.insert(customer);

        Customer customer2 = new Customer();
        customer2.setCustomerId(customer.getCustomerId());

        query.read(customer2);

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

            command.delete(customer1); // in case it's already there.
            command.insert(customer1);

            String id = customer1.getCustomerId();

            Customer customer2 = new Customer();
            customer2.setCustomerId(id);
            query.read(customer2);

            // todo readObject should fail if ID not found??? this test is useless, read returns boolean
            assertNotNull("cust should be found ", customer2);

            long dateRegistered = customer1.getDateRegistered().getTime();

            customer1.setCountry("CA");
            customer1.setDateRegistered(null);

            assertEquals("Customer 1 country should be CA ", "CA", customer1.getCountry());
            assertEquals("Customer 1 date registered should be null", null, customer1.getDateRegistered());


            query.read(customer1);

            assertEquals("Customer 1 country should be US ", "US", customer1.getCountry());
            // we cannot test long. Need to format a date and compare as string to the seconds or minutes because SQL does not store dates with exact accuracy
            log.info(new Date(dateRegistered) + " = ? " + new Date(customer1.getDateRegistered().getTime()));
            assertEquals("Customer 1 date registered should be something?", "" + new Date(dateRegistered), "" + new Date(customer1.getDateRegistered().getTime()));
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

        command.delete(customer); // i case it already exists.
        command.insert(customer);

        customer.setRegion(Regions.North);
        command.update(customer);

//
        boolean failOnMissingProperties = false;

        try {
            query.readList(Customer.class, "SELECT Country from CUSTOMERS");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        List<Customer> list = query.readList(Customer.class, "SELECT company_NAME, Date_Of_Last_ORDER, contact_title, pHone, rEGion, postal_CODE, FAX, DATE_Registered, ADDress, CUStomer_id, Contact_name, country, city from CUSTOMERS");

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
            command.insert(c1);

            Customer c2 = new Customer();
            c2.setCustomerId("456");
            c2.setCompanyName("XYZ INC");
            command.insert(c2);

            Order order;
            order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("ORDER 1");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            order.setPaid(true);
            command.insert(order);

            assertTrue("order # > 0", order.getId() > 0);

            List<Order> orders = query.readList(Order.class, "select * from orders");
            assertEquals("should have 1 order", 1, orders.size());
            assertTrue("order id s/b > 0", orders.get(0).getId() > 0);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("ORDER 2");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            command.insert(order);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("456");
            order.setName("ORDER 3");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            command.insert(order);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("456");
            order.setName("ORDER 4");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            command.insert(order);

            String sql = sb.toString();
            log.info(sql);

            List<CustomerOrder> results = query.readList(CustomerOrder.class, sql);
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

        command.delete(customer); // i case it already exists.
        command.insert(customer);

        List<String> list = query.readList(String.class, "SELECT Country from CUSTOMERS");

        assertEquals("list should have 1", 1, list.size());
        assertEquals("String should be US", "US", list.get(0));

        String country = query.read(String.class, "SELECT Country from CUSTOMERS");
        assertEquals("String should be US", "US", country);

        country = "NOT US";

        country = query.read(String.class, "SELECT Country from CUSTOMERS");

        assertEquals("String should be US", "US", country);

        List<Date> dates = query.readList(Date.class, "select Date_Registered from Customers ");
        log.info(dates);

        Date dt = query.read(Date.class, "select Date_Registered from Customers ");
        log.info(dt);

        // Fails because there is no way to instantiate java.sql.Date - no default constructor.
        List<java.sql.Date> sdates = query.readList(java.sql.Date.class, "select Date_Registered from Customers ");
        log.info(sdates);

        java.sql.Date sdt = query.read(java.sql.Date.class, "select Date_Registered from Customers ");
        log.info(sdt);

        // this should fail. We can't do simple read on a primitive
        boolean failed = false;
        try {
            query.read(country);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'Cannot read a primitive type object with this method.'", "Cannot read a primitive type object with this method.", e.getMessage());
        }
        assertTrue("should have thrown the exception", failed);

    }

    protected abstract void createTables() throws SQLException;

}
