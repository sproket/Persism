package net.sf.persism;

import net.sf.persism.categories.ExternalDB;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.util.Properties;

import static net.sf.persism.UtilsForTests.isTableInDatabase;
import static net.sf.persism.UtilsForTests.isViewInDatabase;

// placeholder
@Category(ExternalDB.class)
public class TestInformix extends BaseTest {

    /*
        https://stackoverflow.com/questions/67550979/trying-to-connect-to-ibms-informix-docker-edition-with-jdbc
        docker run -it --name ifx -h ifx --privileged -p 9088:9088 -p 9089:9089 -p 27017:27017 -p 27018:27018 -p 27883:27883 -e LICENSE=accept ibmcom/informix-developer-database:latest


        https://www.ibm.com/docs/en/informix-servers/14.10?topic=docker-creating-informix-container
        docker run -it --name ifx -h ifx --privileged -e LICENSE=accept -p 9088:9088 -p 9089:9089 -p 27017:27017 -p 27018:27018 -p 27883:27883 icr.io/informix/informix-developer-database:latest
     */

    private static final Log log = Log.getLogger(TestInformix.class);

    @Override
    public void setUp() throws Exception {
        connectionType = ConnectionType.Informix;
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
        log.info("DRIVER: " + con.getMetaData().getDatabaseProductName() + " | " + con.getMetaData().getDatabaseProductVersion());
        session = new Session(con);

        createTables();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
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

        executeCommand(sql, con);

        if (isViewInDatabase("CUSTOMERINVOICE", con)) {
            executeCommand("DROP VIEW CUSTOMERINVOICE", con);
        }


        if (isTableInDatabase("Customers", con)) {
            executeCommand("DROP TABLE Customers", con);
        }

        executeCommand("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " GROUP_ID INT NULL, " +
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

        sql = """
              CREATE TABLE Invoices (
                Invoice_ID SERIAL PRIMARY KEY, 
                Customer_ID varchar(10) NOT NULL,  
                Paid CHAR(1) NOT NULL,  
                Price NUMERIC(7,3) NOT NULL,  
                ActualPrice NUMERIC(7,3) NOT NULL,  
                Status CHAR(1) DEFAULT '1',  
                Created datetime year to fraction(5) DEFAULT current YEAR TO fraction(5) NOT NULL,  
                Quantity NUMERIC(10) NOT NULL,  
                Discount NUMERIC(10,3) NOT NULL )                
        """;
        executeCommand(sql, con);

        sql = """
                CREATE VIEW CustomerInvoice AS
                 SELECT c.Customer_ID, c.Company_Name, i.Invoice_ID, i.Status, i.Created AS DateCreated, i.PAID, i.Quantity
                       FROM Invoices i
                       JOIN Customers c ON i.Customer_ID = c.Customer_ID
                """;
        executeCommand(sql, con);

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
                "   PartnerID varchar(36) NOT NULL, " + // Informix does not support byte types as fields like this. Throws Blobs are not allowed in this expression.
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

        if (isTableInDatabase("InvoiceLineItems", con)) {
            executeCommand("DROP TABLE InvoiceLineItems", con);
        }
        sql = """
                CREATE TABLE InvoiceLineItems (
                    ID SERIAL PRIMARY KEY,
                    INVOICE_ID int,
                    Product_ID int,
                    Quantity int
                    )
                """;
        executeCommand(sql, con);

        if (isTableInDatabase("Products", con)) {
            executeCommand("DROP TABLE Products", con);
        }
        sql = """
                CREATE TABLE Products (
                    ID int,
                    Description VARCHAR(50),
                    BadNumber VARCHAR(30),
                    BadDate VARCHAR(30),
                    BadTimeStamp VARCHAR(30),
                    COST NUMERIC(10,3)
                    )
                """;
        executeCommand(sql, con);

        if (isTableInDatabase("SavedGames", con)) {
            executeCommand("DROP TABLE SavedGames", con);
        }

        executeCommand("CREATE TABLE SavedGames ( " +
                " ID VARCHAR(20) PRIMARY KEY, " +
                " Name VARCHAR(100), " +
                " Some_Date_And_Time DateTime year to fraction(5) NULL, " +
                " Platinum REAL NULL, " +
                " Gold REAL NULL, " +
                " Silver REAL NULL, " +
                " Copper REAL NULL, " +
                " Data CLOB NULL, " +
                " WhatTimeIsIt DateTime year to fraction(5) NULL, " +
                " SomethingBig BLOB NULL) ", con);

        if (isTableInDatabase("Postman", con)) {
            executeCommand("DROP TABLE Postman", con);
        }
        sql = """
                CREATE TABLE Postman (
                    AUTO VARCHAR(50),
                    Host VARCHAR(50),
                    Port NUMERIC(8),
                    User VARCHAR(50),
                    Password VARCHAR(50),
                    missingGetter NUMERIC(10,3)
                    )
                """;
        executeCommand(sql, con);

        // Test for Y ending table who's plural isn't ies....
        if (isTableInDatabase("CorporateHolidays", con)) {
            executeCommand("DROP TABLE CorporateHolidays", con);
        }
        sql = """
                CREATE TABLE CorporateHolidays (
                    ID varchar(10),
                    NAME varchar(40),
                    DATE date
                    )
                """;
        executeCommand(sql, con);

        if (isTableInDatabase("TABLENOPRIMARY", con)) {
            executeCommand("DROP TABLE TABLENOPRIMARY", con);
        }

        executeCommand("CREATE TABLE TABLENOPRIMARY (  ID INT,  Name VARCHAR(30),  Field4 VARCHAR(30),  Field5 DATE,  Field6 INT,  Field7 INT,  Field8 INT )", con);
    }

    public void testSomething() throws Exception {
    }

}
