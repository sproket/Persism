/**
 * Comments for TestMySQL go here.
 *
 * @author Dan Howard
 * @since 6/4/12 9:52 PM
 */
package net.sf.persism;

import net.sf.persism.dao.Contact;
import net.sf.persism.dao.Customer;
import net.sf.persism.dao.Regions;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class TestMySQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMySQL.class);

    // TODO UUID https://mysqlserverteam.com/storing-uuid-values-in-mysql-tables/
    // TODO same as MSSQL - add a flag to use mariadb driver - see downloads folder - Can't. Doesn't install on my win7 vm.
    protected void setUp() throws Exception {

        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/mysql.properties"));

        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");

        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        createTables();

        session = new Session(con);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void createTables() throws SQLException {

        List<String> commands = new ArrayList<String>(12);

        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }
        if (UtilsForTests.isTableInDatabase("Orders", con)) {
            commands.add("DROP TABLE Orders");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " + // todo MySQL Enum type ? https://dev.mysql.com/doc/refman/8.0/en/enum.html
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NOT NULL DEFAULT 'US', " +
                " STATUS CHAR(1) NULL, " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Date_Registered TIMESTAMP, " +
                " Date_Of_Last_Order DATETIME NULL " +
                ") ");

        commands.add("CREATE TABLE Orders ( " +
                " ID INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(ID), " +
                " NAME VARCHAR(30) NULL, " +
                " PAID BIT NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " Created TIMESTAMP, " +
                " Date_Paid DATE NULL, " +
                " Date_Something DATE NULL" +
                ") ");

        executeCommands(commands, con);

        if (UtilsForTests.isTableInDatabase("Contacts", con)) {
            executeCommand("DROP TABLE Contacts", con);
        }

        // https://mysqlserverteam.com/storing-uuid-values-in-mysql-tables/
        String sql = "CREATE TABLE Contacts( " +
                "   identity binary(16) NOT NULL, PRIMARY KEY(identity), " +  // test binary(16)
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
                "   StateProvince varchar(50) NULL, " +
                "   ZipPostalCode varchar(10) NULL, " +
                "   Country nvarchar(50) NULL, " +
                "   DateAdded Date NULL, " +
                "   LastModified DateTime NULL, " +
                "   Notes text NULL, " +
                "   AmountOwed FLOAT NULL, " +
                "   WhatTimeIsIt TIME NULL) ";

        executeCommand(sql, con);
    }

    @Override
    public void testContactTable() throws SQLException {

        UUID identity = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();

        Contact contact = new Contact();
        contact.setIdentity(identity);
        contact.setPartnerId(partnerId);
        contact.setFirstname("Fred");
        contact.setLastname("Flintstone");
        contact.setDivision("DIVISION X");
        contact.setLastModified(new Timestamp(System.currentTimeMillis() - 100000000l));
        contact.setContactName("Fred Flintstone");
        contact.setAddress1("123 Sesame Street");
        contact.setAddress2("Appt #0 (garbage can)");
        contact.setCompany("Grouch Inc");
        contact.setCountry("US");
        contact.setCity("Philly?");
        contact.setType("X");
        contact.setDateAdded(new Date(System.currentTimeMillis()));
        contact.setAmountOwed(100.23f);
        contact.setNotes("B:AH B:AH VBLAH\r\n BLAH BLAY!");
        contact.setWhatTimeIsIt(Time.valueOf(LocalTime.now()));
        session.insert(contact);

        Contact contact2 = new Contact();
        contact2.setIdentity(identity);
        assertTrue(session.fetch(contact2));
        assertNotNull(contact2.getPartnerId());
        assertEquals(contact2.getIdentity(), identity);
        assertEquals(contact2.getPartnerId(), partnerId);

        contact2.setDivision("Y");
        session.update(contact2);

        contact2.setDivision("Y"); // test no update catch
        session.update(contact2);

        assertEquals("1?", 1, session.delete(contact));
    }

    public void testSomething() {
        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setContactName("Fred");
        customer.setRegion(Regions.East);
        customer.setStatus('1');
        customer.setAddress("123 Sesame Street");

        session.insert(customer);

        List<Customer> customers = session.query(Customer.class, "SELECT * FROM CUSTOMERS");
        log.info(customers);

        String result = session.fetch(String.class, "select `Contact_Name` from Customers where Customer_ID = ?", 123);
        log.info(result);
        assertEquals("should be Fred", "Fred", result);

        int count = session.fetch(int.class, "select count(*) from Customers where Region = ?", Regions.East);
        assertEquals("should be 1", 1, count);
        log.info("count " + count);
    }
}
