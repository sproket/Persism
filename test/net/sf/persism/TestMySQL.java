package net.sf.persism;

import net.sf.persism.dao.Customer;
import net.sf.persism.dao.Order;
import net.sf.persism.dao.Regions;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Dan Howard
 * @since 6/4/12 9:52 PM
 */
public final class TestMySQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMySQL.class);

    // TODO MariaDB? same as MSSQL - add a flag to use mariadb driver - see downloads folder - Can't. Doesn't install on my win7 vm.

    @Override
    protected void setUp() throws Exception {
        connectionType = ConnectionTypes.MySQL;
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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void testContactTable() throws SQLException {
        super.testContactTable();
        assertTrue(true);
    }

    @Override
    protected void createTables() throws SQLException {

        this.connectionType = connectionType;

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
                " STATUS CHAR(1) NOT NULL DEFAULT ' ', " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Date_Registered TIMESTAMP, " +
                " Date_Of_Last_Order DATETIME NULL, " +
                " TestLocalDate DATETIME NULL, " +
                " TestLocalDateTime TIMESTAMP NULL " +
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
                "   LastModified DATETIME NULL, " +
                "   Notes text NULL, " +
                "   AmountOwed FLOAT NULL, " +
                "   SOME_DATE TIMESTAMP NULL, " +
                "   tesTInstanT DateTime NULL, " +
                "   tesTInstanT2 TIMESTAMP NULL, " +
                "   WhatTimeIsIt TIME NULL) ";

        executeCommand(sql, con);
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
