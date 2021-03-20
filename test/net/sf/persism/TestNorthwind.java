package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.categories.LocalDB;
import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.northwind.*;
import org.junit.ClassRule;
import org.testcontainers.containers.MSSQLServerContainer;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


/**
 * Does not share common tests - this is just to do some specific tests on SQL with Northwind DB
 *
 * @author Dan Howard
 * @since 5/3/12 8:46 PM
 */
@org.junit.experimental.categories.Category(ExternalDB.class)
public class TestNorthwind extends TestCase {

    private static final Log log = Log.getLogger(TestNorthwind.class);

    Connection con;

    Session session;

    protected void setUp() throws Exception {
        super.setUp();
        //log.error(log.getLogName() + " " + log.getLogMode());

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/northwind.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        session = new Session(con);
    }


    @Override
    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }

    public void testBinaryImage() {

        Category category = new Category();
        category.setCategoryId(1);
        boolean found = session.fetch(category);
        assertTrue(found);


        // Images come across as byte arrays
        try {
            List<Category> list = session.query(Category.class, "select * from categories");

            for (Category cat : list) {
                log.info(cat);
                File directory = new File("c:/temp/pinf/");
                if (!directory.exists()) {
                    directory.mkdir();
                }

                File file = new File("c:/temp/pinf/" + cat.getCategoryId() + ".jpg");
                ImageIO.write(cat.getImage(), "jpg", file);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //fail(e.getMessage());
        }

    }

    public void testCustomers() {
        try {

            Customer customer = new Customer();
            customer.setCustomerId("XXYYZ");
            session.delete(customer); // make sure this dup test is deleted


            // Test insert before read

            customer = new Customer();
            customer.setCustomerId("BRUBL");

            customer.setCompanyName("ABC INC");
            customer.setContactName("Barney Rubble");
            session.insert(customer);

            // test delete
            session.delete(customer);


            List<Customer> list = session.query(Customer.class, "select top 10 * from Customers");

            for (Customer cust : list) {
                log.info(cust);
            }


            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rs = dmd.getPrimaryKeys(null, null, "Customers");

            while (rs.next()) {
                log.info("primary key(s) " + rs.getString("COLUMN_NAME"));
            }
            rs.close();


            // Test customer - this has a primary key on CustomerID but this column is not generated.
            // The user needs to set some unique value.
            customer = new Customer();
            customer.setCompanyName("ABC INC");
            customer.setContactName("Fred Flintstone");

            try {
                session.insert(customer);
            } catch (PersismException e) {
                assertEquals("should have insert exception here", "Cannot insert the value NULL into column 'CustomerID', table 'Northwind.dbo.Customers'; column does not allow nulls. INSERT fails.", e.getMessage());
            }

            // Test customer duped key
            customer.setCustomerId("XXYYZ");
            session.insert(customer); // should not fail

            // Test customer duped key
            boolean dupfail = false;
            try {
                customer.setCustomerId("XXYYZ");
                session.insert(customer); // this should fail
            } catch (PersismException e) {
                dupfail = true;
                log.info(e.getMessage());
            }
            assertTrue("duped key failed - like we wanted....", dupfail);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }


    static final String ORDER_QUERY = "SELECT o.OrderID, o.CustomerID, o.EmployeeID, o.OrderDate, " +
            "o.RequiredDate, o.ShippedDate, d.ProductID,  " +
            "d.UnitPrice, d.Quantity, d.Discount,  " +
            "e.LastName + ', ' + e.FirstName EmployeeName, " +
            "c.CompanyName CustomerName, " +
            "p.ProductName " +
            "FROM Orders o " +
            "JOIN \"Order Details\" d ON o.OrderID = d.OrderID " +
            "JOIN Employees e ON o.EmployeeID = e.EmployeeID " +
            "JOIN Customers c ON o.CustomerID = c.CustomerID " +
            "JOIN Products p ON d.ProductID = p.ProductID ";

    public void testQuery() {

        long now = System.currentTimeMillis();
        List<OrderView> list = session.query(OrderView.class, ORDER_QUERY);
        log.info("time to get list " + (System.currentTimeMillis() - now));


        // log.info(list);
        log.info("rows: " + list.size());

        now = System.currentTimeMillis();
        list = session.query(OrderView.class, ORDER_QUERY);
        log.info("time to get list " + (System.currentTimeMillis() - now));

        now = System.currentTimeMillis();
        list = session.query(OrderView.class, ORDER_QUERY);
        log.info("time to get list " + (System.currentTimeMillis() - now));

    }

    public void testOrderDetail() {

        java.sql.ResultSet rs = null;
        try {
            DatabaseMetaData dmd = con.getMetaData();

            rs = dmd.getColumns(null, null, "Order Details", null);
            while (rs.next()) {
                log.info("testOrderDetail: " + rs.getString("COLUMN_NAME"));
            }
            rs.close();

            rs = dmd.getPrimaryKeys(null, null, "Order Details");
            while (rs.next()) {
                log.info("testOrderDetail: " + rs.getString("COLUMN_NAME"));
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            Util.cleanup(rs);
        }

    }

    public void testSpaceInTableName() {

        try {
            DatabaseMetaData dbmd = con.getMetaData();
            log.info("CATALOG getCatalogSeparator? " + dbmd.getCatalogSeparator());
            log.info("CATALOG getCatalogTerm? " + dbmd.getCatalogTerm());
            log.info("CATALOG getExtraNameCharacters? " + dbmd.getExtraNameCharacters());
            log.info("CATALOG getIdentifierQuoteString? " + dbmd.getIdentifierQuoteString());
            log.info("CATALOG getSearchStringEscape? " + dbmd.getSearchStringEscape());
            log.info("CATALOG getSQLKeywords? " + dbmd.getSQLKeywords());
            // Throws java.lang.AbstractMethodError
            // log.info("CATALOG autoCommitFailureClosesAllResultSets? " + dbmd.autoCommitFailureClosesAllResultSets()); // JDBC4? remove if compiling back in Java6

            Customer wontWork = new Customer();
            wontWork.setCompanyName("FAIL");
            try {
                if (!session.fetch(wontWork)) {
                    session.insert(wontWork);
                }

            } catch (PersismException e) {
                assertEquals("msg s/b 'Cannot insert the value NULL into column 'CustomerID', table 'Northwind.dbo.Customers'; column does not allow nulls. INSERT fails.'",
                        "Cannot insert the value NULL into column 'CustomerID', table 'Northwind.dbo.Customers'; column does not allow nulls. INSERT fails.",
                        e.getMessage());
            }

            // Order Details
            Customer customer = new Customer();
            customer.setCustomerId("MOO");

            if (!session.fetch(customer)) {
                customer.setCompanyName("TEST");
                customer.setAddress("123 sesame street");
                customer.setCity("city");
                customer.setContactName("fred flintstone");
                customer.setContactTitle("Lord");
                customer.setCountry("US");
                customer.setFax("123-456-7890");
                customer.setPhone("456-678-1234");
                customer.setPostalCode("54321");
                customer.setRegion("East");
                customer.setDateOfLastResort(new Date(System.currentTimeMillis()));
                session.insert(customer);
            } else {
                customer.setDateOfLastResort(new Date(System.currentTimeMillis()));
                session.update(customer);
// remove orders and details for 'MOO'
                List<Order> orders = session.query(Order.class, "select * from orders where customerID=?", "MOO");
                for (Order order : orders) {
                    session.execute("DELETE FROM \"ORDER Details\" WHERE OrderID=?", order.getOrderId());
                }
                session.execute("DELETE FROM ORDERS WHERE CustomerID=?", "MOO");
            }
            log.warn("DATE? " + customer.getDateOfLastResort());
            session.fetch(customer);
            log.warn("CUSTOMER DATE? " + customer);

            // Find employee to place this order
            // Leverling	Janet
            Employee employee = session.fetch(Employee.class, "SELECT * FROM Employees WHERE LastName=? and FirstName=?", "Leverling", "Janet");

            assertTrue("employee should be found ", employee != null && employee.getEmployeeId() > 0);
            employee.setStatus('a');
            employee.setWhatTimeIsIt(new Time(System.currentTimeMillis()));
            session.update(employee);

            employee = session.fetch(Employee.class, "SELECT * FROM Employees WHERE LastName=? and FirstName=?", "Leverling", "Janet");
            assertNotNull(employee);
            assertEquals("status s/b 'a'", 'a', employee.getStatus());

            // Find shipper
            Shipper shipper = session.fetch(Shipper.class, "SELECT * FROM Shippers WHERE CompanyName=?", "Speedy Express");
            assertTrue("Shipper should be found ", shipper != null && shipper.getShipperId() > 0);


            Order order = new Order();
            order.setCustomerId(customer.getCustomerId());
            order.setEmployeeId(employee.getEmployeeId());
            order.setShipVia(shipper.getShipperId());
            order.setOrderDate(new java.util.Date());
            order.setShipAddress("123 Sesame Street");
            order.setShipCity("Some City");
            order.setShipCountry("US");
            order.setShipName("Barney Rubble");
            order.setShipPostalCode("04132");
            order.setShipRegion("?");

            session.insert(order);

            List<Product> products = session.query(Product.class, "select * from Products where ProductName like ?", "%Cranberry%");
            assertEquals("should have 1", 1, products.size());

            Product product = products.get(0);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(product.getProductId());
            orderDetail.setOrderId(order.getOrderId());
            //orderDetail.setDiscount(new BigDecimal(".23"));
            orderDetail.setDiscount(.23d);
            orderDetail.setQuantity(100);
//            orderDetail.setUnitPrice(new BigDecimal("10.32"));
            orderDetail.setUnitPrice(10.32d);

            session.insert(orderDetail);

            // query back

//            orderDetail.setDiscount(null);
            orderDetail.setDiscount(0);
            orderDetail.setQuantity(0);
//            orderDetail.setUnitPrice(null);
            orderDetail.setUnitPrice(0);
            session.fetch(orderDetail);
//            assertEquals("discount should be 0.23", "0.23", "" + orderDetail.getDiscount().setScale(2, BigDecimal.ROUND_HALF_EVEN));
            DecimalFormat f = new DecimalFormat("#0.00");
            assertEquals("discount should be 0.23", "0.23", "" + f.format(orderDetail.getDiscount()));
            assertEquals("quantity should be 100", 100, orderDetail.getQuantity());
//            assertEquals("unit price should be 10.3200", "10.3200", "" + f.format(orderDetail.getUnitPrice()));
            assertEquals("unit price should be 10.32", "10.32", "" + f.format(orderDetail.getUnitPrice()));

//            orderDetail.setDiscount(BigDecimal.valueOf(.50d));
            orderDetail.setDiscount(.50d);
            session.update(orderDetail);

//            orderDetail.setDiscount(null);
            orderDetail.setDiscount(0);
            session.fetch(orderDetail);
            //assertEquals("discount should be 0.50", "0.50", "" + orderDetail.getDiscount().setScale(2, BigDecimal.ROUND_HALF_EVEN));
            assertEquals("discount should be 0.50", "0.50", "" + f.format(orderDetail.getDiscount()));


            assertEquals("delete should return 1", 1, session.delete(orderDetail));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testQueryWithSpecificColumnsWhereCaseDoesNotMatch() throws SQLException, InterruptedException {

        log.info("testQueryWithSpecificColumnsWhereCaseDoesNotMatch with : " + con.getMetaData().getURL());

        session.execute("DELETE FROM ORDERS WHERE CustomerID='MOO'");

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        customer.setCountry("US");
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion("East");
        customer.setDateOfDoom(new Date(System.currentTimeMillis()));
        customer.setDateOfOffset(LocalDateTime.now());
        customer.setDateOfLastResort(new Date(System.currentTimeMillis()));
        customer.setTestLocalDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        customer.setNowMF(LocalDateTime.now());
        customer.setWtfDate("1998-02-17 10:43:22");
        session.delete(customer); // in case it already exists.
        session.insert(customer);
        Thread.sleep(100l);
        customer.setRegion("North");
        customer.setDateOfDoom(new Date(System.currentTimeMillis()));
        customer.setDateOfOffset(LocalDateTime.now());
        customer.setDateOfLastResort(new Date(System.currentTimeMillis()));
        customer.setNowMF(LocalDateTime.now());
        customer.setWtfDate("1999-02-17 10:43:22");
        session.update(customer);

        customer.setRegion(null);
        assertEquals("customer region should be null", null, customer.getRegion());

        // read it back to make sure 'North' was set.
        session.fetch(customer);
        assertEquals("customer region should be north", "North", customer.getRegion());

        boolean failOnMissingProperties = false;
        try {
            session.query(Customer.class, "SELECT Country from CUSTOMERS");
        } catch (Exception e) {
            log.info(e.getMessage());
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        List<Customer> list = session.query(Customer.class, "SELECT companyNAME, contacttitle, pHone, rEGion, postalCODE, FAX, ADDress, CUStomerid, conTacTname, coUntry, cIty, DAteOfLastResort,DATEOFDOOM, wtfDATE, nowMF, TESTLocalDateTime  from CuStOMeRS WHERE CustomerID='MOo'");

        log.info(list);
        assertEquals("list should be 1", 1, list.size());

        Customer c2 = list.get(0);
        assertEquals("region s/b north ", "North", c2.getRegion());

        c2.setWtfDate("WHY WOULD YOU DO THIS?");
        boolean shouldFail = false;

        try {
            session.update(c2);
        } catch (PersismException e) {
            log.info(e.getMessage());
            shouldFail = true;
            assertEquals("s/b 'Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]. Column: wtfDate Target Conversion: class java.sql.Timestamp - Type read: class java.lang.String VALUE: WHY WOULD YOU DO THIS?'",
                    "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]. Column: wtfDate Target Conversion: class java.sql.Timestamp - Type read: class java.lang.String VALUE: WHY WOULD YOU DO THIS?",
                    e.getMessage());
        }
        assertTrue(shouldFail);
    }


}
