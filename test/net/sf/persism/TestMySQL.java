package net.sf.persism;

import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.Customer;
import net.sf.persism.dao.Regions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.MySQLContainer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dan Howard
 * @since 6/4/12 9:52 PM
 */
@Category(TestContainerDB.class)
public final class TestMySQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMySQL.class);

    @ClassRule
    private static final MySQLContainer<?> DB_CONTAINER = new MySQLContainer<>("mysql:5.7.22")
            .withUsername("pinf")
            .withPassword("pinf")
            .withDatabaseName("pinf");

    @Override
    protected void setUp() throws Exception {
        if(!DB_CONTAINER.isRunning()) {
            DB_CONTAINER.start();
        }
        connectionType = ConnectionTypes.MySQL;
        super.setUp();

        Class.forName(DB_CONTAINER.getDriverClassName());

        con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());


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
                " Region ENUM('North', 'South', 'East', 'West'), " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NOT NULL DEFAULT 'US', " +
                " STATUS CHAR(1) NOT NULL DEFAULT ' ', " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Date_Registered TIMESTAMP, " +
                " Date_Of_Last_Order DATE NULL, " +
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
                "   Status TINYINT NOT NULL, " +
                "   DateAdded Date NULL, " +
                "   LastModified DATETIME NULL, " +
                "   Notes text NULL, " +
                "   AmountOwed FLOAT NULL, " +
                "   `BigInt` DECIMAL(20) NULL, " +
                "   SOME_DATE TIMESTAMP NULL, " +
                "   tesTInstanT DateTime NULL, " +
                "   tesTInstanT2 TIMESTAMP NULL, " +
                "   WhatMiteIsIt TIME NULL, " +
                "   WhatTimeIsIt TIME NULL) ";

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


    }

    public void testSomething() {
        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setContactName("Fred");
        customer.setRegion(Regions.East);
        customer.setStatus('1');
        customer.setAddress("123 Sesame Street");

        session.insert(customer);

        List<Customer> customers = session.query(Customer.class, "SELECT * FROM Customers");
        log.info(customers);

        String result = session.fetch(String.class, "select `Contact_Name` from Customers where Customer_ID = ?", 123);
        log.info(result);
        assertEquals("should be Fred", "Fred", result);

        int count = session.fetch(int.class, "select count(*) from Customers where Region = ?", Regions.East);
        assertEquals("should be 1", 1, count);
        log.info("count " + count);

    }

    @Override
    public void testAllDates() {
        super.testAllDates();
    }
}
