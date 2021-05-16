package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static net.sf.persism.UtilsForTests.isTableInDatabase;

// placeholder
@Category(ExternalDB.class)
public class TestInformix extends BaseTest {

    private static final Log log = Log.getLogger(TestInformix.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();


        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/informix.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        log.warn(driver);

        log.warn(url);

        con = DriverManager.getConnection(url, username, password);

        session = new Session(con);

        createTables();
    }

    @Override
    protected void createTables() throws SQLException {
        String sql;

        if (isTableInDatabase("Orders", con)) {
            sql = "DROP TABLE Orders";
            executeCommand(sql, con);
        }

        sql = "CREATE TABLE orders ( " +
                " id serial not null, " + //  PRIMARY KEY
                " NAME VARCHAR(30), " +
                " PAID CHAR(1), " +
                " Prepaid CHAR(1)," +
                " IsCollect CHAR(1)," +
                " IsCancelled CHAR(1)," +
                " Customer_ID VARCHAR(10), " +
                " Created datetime year to fraction(5) DEFAULT current YEAR TO fraction(5) NOT NULL, " +
                " Date_Paid datetime year to fraction(5), " +
                " Date_Something datetime year to second" +
                ") ";

        // date - default today

        //ALTER TABLE sysmaster:informix.orderstest ADD date_something datetime year to second;

        // ALTER TABLE sysmaster:informix.orderstest ADD date1 datetime year to fraction(5) DEFAULT current YEAR TO fraction(5);

/*
CREATE TABLE sysmaster:informix.orderstest (
	id serial NOT NULL,
	name varchar(30) NOT NULL,
	paid char(1),
	prepaid char(1),
	iscollect char(1),
	iscancelled char(1),
	customer_id varchar(10),
	created datetime year to fraction(5) DEFAULT TODAY NOT NULL,
	date_paid date,
	date_something datetime year to fraction(5)
);

CREATE TABLE sysmaster:informix.orderstest (
	id serial NOT NULL,
	name varchar(30) NOT NULL,
	paid byte(1),
	prepaid char(1),
	iscollect char(1),
	iscancelled byte(1),
	customer_id varchar(10)
);

 */
        executeCommand(sql, con);

        if (isTableInDatabase("Customers", con)) {
            executeCommand("DROP TABLE Customers", con);
        }

        executeCommand("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30), " +
                " Contact_Name VARCHAR(30), " +
                " Contact_Title VARCHAR(10), " +
                " Address VARCHAR(40), " +
                " City VARCHAR(30), " +
                " Region VARCHAR(10), " +
                " Postal_Code VARCHAR(10), " +
                " Country VARCHAR(2) NOT NULL DEFAULT 'US', " +
                " Phone VARCHAR(30), " +
                " Fax VARCHAR(30), " +
                " Status CHAR(1), " +
                " Date_Registered datetime year to fraction(5) DEFAULT current YEAR TO fraction(5) NOT NULL, " +
                " Date_Of_Last_Order DATE, " +
                " TestLocalDate date, " +
                " TestLocalDateTime datetime year to fraction(5)" +
                ") ", con);

        if (isTableInDatabase("Invoices", con)) {
            executeCommand("DROP TABLE Invoices", con);
        }

        executeCommand("CREATE TABLE Invoices ( " +
                " Invoice_ID SERIAL PRIMARY KEY, " +
                " Customer_ID varchar(10) NOT NULL, " +
                " Paid CHAR(1) NOT NULL, " +
                " Price NUMERIC(7,3) NOT NULL, " +
                " ActualPrice NUMERIC(7,3) NOT NULL, " +
                " Status INT DEFAULT 1, " +
                " Created datetime year to fraction(5) DEFAULT current YEAR TO fraction(5) NOT NULL, " +
                " Quantity NUMERIC(10) NOT NULL, " +
                " Discount NUMERIC(10,3) NOT NULL " +
                ") ", con);

        if (isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            executeCommand("DROP TABLE TABLEMULTIPRIMARY", con);
        }

        if (isTableInDatabase("SavedGames", con)) {
            executeCommand("DROP TABLE SavedGames", con);
        }

        executeCommand("CREATE TABLE TABLEMULTIPRIMARY ( " +
                " OrderID INT NOT NULL, " +
                " ProductID VARCHAR(10) NOT NULL, " +
                " UnitPrice DECIMAL NOT NULL, " +
                " Quantity SMALLINT NOT NULL, " +
                " Discount REAL NOT NULL " +
                ") ", con);

        // ALTER TABLE sysmaster:informix.multiprimary ADD CONSTRAINT PRIMARY KEY (orderid,productid) CONSTRAINT multiprimary_pk;
        executeCommand("ALTER TABLE TABLEMULTIPRIMARY ADD CONSTRAINT PRIMARY KEY (orderid,productid) CONSTRAINT TABLEMULTIPRIMARY_pk", con);

        executeCommand("CREATE TABLE SavedGames ( " +
                " ID VARCHAR(20) PRIMARY KEY, " +
                " Name VARCHAR(100), " +
                " Some_Date_And_Time datetime year to fraction(5), " +
                " Gold REAL, " +
                " Silver REAL, " +
                " Copper REAL, " +
                " Data TEXT, " +
                " WhatTimeIsIt datetime year to fraction(5), " +
                " SomethingBig BLOB NULL) ", con); // blob?

        if (isTableInDatabase("Contacts", con)) {
            executeCommand("DROP TABLE Contacts", con);
        }

        sql = "CREATE TABLE Contacts( " +
                "   identity varchar(36) NOT NULL PRIMARY KEY, " + // varchar is safest
                "   PartnerID varchar(36) NOT NULL, " +
                "   Type char(2) NOT NULL, " +
                "   Firstname varchar(50) NOT NULL, " +
                "   Lastname varchar(50) NOT NULL, " +
                "   ContactName varchar(50) NOT NULL, " +
                "   Company varchar(50) NOT NULL, " +
                "   Division varchar(50), " +
                "   Email varchar(50), " +
                "   Address1 varchar(50), " +
                "   Address2 varchar(50), " +
                "   City varchar(50), " +
                "   Status smallint, " + // tiny on others...
                "   StateProvince varchar(50), " +
                "   ZipPostalCode varchar(10), " +
                "   Country varchar(50), " +
                "   DateAdded Date, " +
                "   LastModified DateTime year to fraction(5), " +
                "   Notes text, " +
                "   AmountOwed REAL, " +
                "   BigInt DECIMAL, " +
                "   Some_DATE DateTime year to fraction(5), " +
                "   TestInstant DateTime year to fraction(5), " +
                "   TestInstant2 DATE, " +
                "   WhatMiteIsIt DateTime year to fraction(5), " +
                "   WhatTimeIsIt DateTime year to fraction(5)) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTest", con)) {
            executeCommand("DROP TABLE DateTest", con);
        }

        sql = "CREATE TABLE DateTest ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " SqlDate1 DateTime year to fraction(5), " +
                " SqlDate2 DATE, " +
                " LocalDate1 DateTime year to fraction(5), " +
                " LocalDate2 DATE, " +
                " UtilDate1 DateTime year to fraction(5), " +
                " UtilDate2 DATE, " +
                " Instant1 DateTime year to fraction(5), " +
                " Instant2 DATE, " +
                " Timestamp1 DateTime year to fraction(5), " +
                " Timestamp2 DATE, " +
                " LocalDateTime1 DateTime year to fraction(5), " +
                " LocalDateTime2 DATE, " +
                " Time1 DateTime year to fraction(5)," +
                " Time2 DateTime year to fraction(5)," +
                " LocalTime1 DateTime year to fraction(5)," +
                " LocalTime2 DateTime year to fraction(5)) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTestLocalTypes", con)) {
            executeCommand("DROP TABLE DateTestLocalTypes", con);
        }

        sql = "CREATE TABLE DateTestLocalTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly DateTime year to fraction(5)," +
                " DateAndTime DateTime year to fraction(5)) ";

        executeCommand(sql, con);

        if (isTableInDatabase("DateTestSQLTypes", con)) {
            executeCommand("DROP TABLE DateTestSQLTypes", con);
        }

        sql = "CREATE TABLE DateTestSQLTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly DateTime year to fraction(5)," +
                " UtilDateAndTime DateTime year to fraction(5)," +
                " DateAndTime DateTime year to fraction(5)) ";

        executeCommand(sql, con);

        if (isTableInDatabase("ByteData", con)) {
            executeCommand("DROP TABLE ByteData", con);
        }
        sql = "CREATE TABLE ByteData ( " +
                "ID VARCHAR(1), " +
                "BYTE1 INT, " +
                "BYTE2 INT ) ";
        executeCommand(sql, con);

        if (isTableInDatabase("RecordTest1", con)) {
            executeCommand("DROP TABLE RecordTest1", con);
        }
        sql = "CREATE TABLE RecordTest1 ( " +
                "ID VARCHAR(36), " +
                "NAME VARCHAR(20), " +
                "QTY INT, " +
                "PRICE REAL " +
                ") ";
        executeCommand(sql, con);

        if (isTableInDatabase("RecordTest2", con)) {
            executeCommand("DROP TABLE RecordTest2", con);
        }
        sql = "CREATE TABLE RecordTest2 ( " +
                "ID SERIAL PRIMARY KEY, " +
                "DESCRIPTION VARCHAR(20), " +
                "QTY INT, " +
                "PRICE REAL, " +
                "CREATED_ON DATE default TODAY" + // was datetime..... might need it to be...
                ") ";
        executeCommand(sql, con);

    }


    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSomething() throws Exception {
    }

}
