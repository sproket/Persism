/**
 * Comments for TestNorthwind go here.
 * @author Dan Howard
 * @since 5/3/12 8:46 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.northwind.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Properties;

// Does not share common tests - this is just to do some specific tests on SQL with Northwind DB
public class TestNorthwind extends TestCase {

    private static final Log log = Log.getLogger(TestNorthwind.class);

    Connection con;
    Query query;
    Command command;

    protected void setUp() throws Exception {
        super.setUp();

        // TODO JTDS 1.2.5 for Java 6 for now
        // TODO JTDS 1.3.1 for Java 8 (see lib folder)s
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/northwind.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        query = new Query(con);
        command = new Command(con);
    }

    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }

    public void testBinaryImage() {
        try {
            List<Category> list = query.readList(Category.class, "select * from categories");

            for (Category cat : list) {
                log.info(cat);
                File directory = new File("c:/temp/pinf/");
                if (! directory.exists()){
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
            command.delete(customer); // make sure this dup test is deleted


            // Test insert before read

            customer = new Customer();
            customer.setCustomerId("BRUBL");

            customer.setCompanyName("ABC INC");
            customer.setContactName("Barney Rubble");
            command.insert(customer);

            // test delete
            command.delete(customer);


            List<Customer> list = query.readList(Customer.class, "select * from Customers");

            for (Customer cust : list) {
                log.info(cust);
            }


            DatabaseMetaData dmd = con.getMetaData();
            java.sql.ResultSet rs = dmd.getPrimaryKeys(null, null, "Customers");

            while (rs.next()) {
                log.info(rs.getString("COLUMN_NAME"));
            }
            rs.close();


            // Test customer - this has a primary key on CustomerID but this column is not generated.
            // The user needs to set some unique value.
            customer = new Customer();
            customer.setCompanyName("ABC INC");
            customer.setContactName("Fred Flintstone");

            try {
                command.insert(customer);
            } catch (PersismException e) {
                assertEquals("should have insert exception here", "java.sql.SQLException: Cannot insert the value NULL into column 'CustomerID', table 'Northwind.dbo.Customers'; column does not allow nulls. INSERT fails.", e.getMessage());
            }

            // Test customer duped key
            customer.setCustomerId("XXYYZ");
            command.insert(customer); // should not fail

            // Test customer duped key
            boolean dupfail = false;
            try {
                customer.setCustomerId("XXYYZ");
                command.insert(customer); // this should fail
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
        List<OrderView> list = query.readList(OrderView.class, ORDER_QUERY);
        log.info("time to get list " + (System.currentTimeMillis() - now));


        // log.info(list);
        log.info("rows: " + list.size());

        now = System.currentTimeMillis();
        list = query.readList(OrderView.class, ORDER_QUERY);
        log.info("time to get list " + (System.currentTimeMillis() - now));

        now = System.currentTimeMillis();
        list = query.readList(OrderView.class, ORDER_QUERY);
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
//            log.info("CATALOG autoCommitFailureClosesAllResultSets? " + dbmd.autoCommitFailureClosesAllResultSets()); // JDBC4?

            // Order Details
            Customer customer = new Customer();
            customer.setCustomerId("MOO");

            if (!query.read(customer)) {
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

                command.insert(customer);
            } else {
                // remove orders and details for 'MOO'
                List<Order> orders = query.readList(Order.class, "select * from orders where customerID=?", "MOO");
                for (Order order : orders) {
                    command.execute("DELETE FROM \"ORDER Details\" WHERE OrderID=?", order.getOrderId());
                }
                command.execute("DELETE FROM ORDERS WHERE CustomerID=?", "MOO");
            }


            // Find employee to place this order
            // Leverling	Janet
            Employee employee = query.read(Employee.class, "SELECT * FROM Employees WHERE LastName=? and FirstName=?", "Leverling", "Janet");

            assertTrue("employee should be found ", employee != null && employee.getEmployeeId() > 0);

            // Find shipper
            Shipper shipper = query.read(Shipper.class, "SELECT * FROM Shippers WHERE CompanyName=?", "Speedy Express");
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

            command.insert(order);

            List<Product> products = query.readList(Product.class, "select * from Products where ProductName like ?", "%Cranberry%");
            assertEquals("should have 1", 1, products.size());

            Product product = products.get(0);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(product.getProductId());
            orderDetail.setOrderId(order.getOrderId());
            orderDetail.setDiscount(new BigDecimal(".23"));
            orderDetail.setQuantity(100);
            orderDetail.setUnitPrice(new BigDecimal("10.32"));

            command.insert(orderDetail);

            // query back

            orderDetail.setDiscount(null);
            orderDetail.setQuantity(0);
            orderDetail.setUnitPrice(null);
            query.read(orderDetail);
            assertEquals("discount should be 0.23", "0.23", "" + orderDetail.getDiscount());
            assertEquals("quantity should be 100", 100, orderDetail.getQuantity());
            assertEquals("unit price should be 10.3200", "10.3200", "" + orderDetail.getUnitPrice());

            assertEquals("delete should return 1", 1, command.delete(orderDetail));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testQueryWithSpecificColumnsWhereCaseDoesNotMatch() throws SQLException {

        log.info("testQueryWithSpecificColumnsWhereCaseDoesNotMatch with : " + con.getMetaData().getURL());

        command.execute("DELETE FROM ORDERS WHERE CustomerID='MOO'");

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

        command.delete(customer); // i case it already exists.
        command.insert(customer);

        customer.setRegion("North");
        command.update(customer);

        customer.setRegion(null);
        assertEquals("customer region should be null", null, customer.getRegion());

        // read it back to make sure 'North' was set.
        query.read(customer);
        assertEquals("customer region should be north", "North", customer.getRegion());

        boolean failOnMissingProperties = false;
        try {
            query.readList(Customer.class, "SELECT Country from CUSTOMERS");
        } catch (Exception e) {
            log.info(e.getMessage());
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        List<Customer> list = query.readList(Customer.class, "SELECT companyNAME, contacttitle, pHone, rEGion, postalCODE, FAX, ADDress, CUStomerid, conTacTname, coUntry, cIty from CuStOMeRS WHERE CustomerID='MOo'");

        log.info(list);
        assertEquals("list should be 1", 1, list.size());

        Customer c2 = list.get(0);
        assertEquals("region s/b north ", "North", c2.getRegion());
    }


}
