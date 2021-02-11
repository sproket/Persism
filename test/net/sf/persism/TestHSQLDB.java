package net.sf.persism;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class TestHSQLDB extends BaseTest {

    private static final Log log = Log.getLogger(TestHSQLDB.class);

    @Override
    public void setUp() throws Exception {
        connectionType = ConnectionTypes.HSQLDB;
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/hsqldb.properties"));
        Class.forName(props.getProperty("database.driver"));

        String home = UtilsForTests.createHomeFolder("pinfhsqldb");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url, "sa", "");

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        createTables();

        session = new Session(con);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void testContactTable() throws SQLException {
        super.testContactTable();
        assertTrue(true);
    }

    @Override
    protected void createTables() throws SQLException {
        List<String> commands = new ArrayList<>(12);
        String sql;

        if (UtilsForTests.isTableInDatabase("Orders", con)) {
            sql = "DROP TABLE Orders";
            commands.add(sql);
            executeCommand(sql, con);
        }

        sql = "CREATE TABLE Orders ( " +
                " ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) PRIMARY KEY, " +
                " NAME VARCHAR(30) NULL, " +
                " PAID BIT NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " Created TIMESTAMP DEFAULT NOW() NOT NULL, " +
                " Date_Paid TIMESTAMP NULL, " +
                " Date_Something TIMESTAMP NULL " +
                ") ";

        commands.add(sql);
        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            sql = "DROP TABLE Customers";
            commands.add(sql);
            executeCommand(sql, con);
        }

        sql = "CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) DEFAULT 'US', " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Status CHAR(1) NULL, " +
                " Date_Registered Timestamp DEFAULT NOW(), " +
                " Date_Of_Last_Order Timestamp, " +
                " TestLocalDate date, " +
                " TestLocalDateTime Timestamp " +

                ") ";
        commands.add(sql);
        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("Invoices", con)) {
            sql = "DROP TABLE Invoices";
            commands.add(sql);
            executeCommand(sql, con);
        }

        sql = "CREATE TABLE Invoices ( " +
                " Invoice_ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) PRIMARY KEY, " +
                " Customer_ID varchar(10) NOT NULL, " +
                " Paid BIT NOT NULL, " +
                " Price NUMERIC(7,3) NOT NULL, " +
                " Quantity NUMERIC(10) NOT NULL, " +
                " Total NUMERIC(10,3) NOT NULL, " +
                " Discount NUMERIC(10,3) NOT NULL " +
                ") ";
        commands.add(sql);
        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("TABLEMULTIPRIMARY", con)) {
            sql = "DROP TABLE TABLEMULTIPRIMARY";
            commands.add(sql);
            executeCommand(sql, con);
        }

        if (UtilsForTests.isTableInDatabase("SavedGames", con)) {
            sql = "DROP TABLE SavedGames";
            commands.add(sql);
            executeCommand(sql, con);
        }

        sql = "CREATE TABLE TABLEMULTIPRIMARY ( " +
                " OrderID INT NOT NULL, " +
                " ProductID INT NOT NULL, " +
                " UnitPrice DECIMAL NOT NULL, " +
                " Quantity SMALLINT NOT NULL, " +
                " Discount REAL NOT NULL " +
                ") ";
        commands.add(sql);
        executeCommand(sql, con);

        sql = "ALTER TABLE TABLEMULTIPRIMARY ADD PRIMARY KEY (OrderID, ProductID)";
        commands.add(sql);
        executeCommand(sql, con);


        sql = "CREATE TABLE SavedGames ( " +
                " ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) PRIMARY KEY, " +
                " Name VARCHAR(100), " +
                " Timestamp TIMESTAMP NULL, " +
                " Gold REAL NULL, " +
                " Silver REAL NULL, " +
                " Data CLOB NULL, " +
                " WhatTimeIsIt Time NULL, " +
                " SomethingBig BLOB NULL) ";
        commands.add(sql);
        executeCommand(sql, con);


        if (UtilsForTests.isTableInDatabase("Contacts", con)) {
            executeCommand("DROP TABLE Contacts", con);
        }

        sql = "CREATE TABLE Contacts( " +
                "   identity binary(16) NOT NULL PRIMARY KEY, " +  // test binary(16)
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
                "   Country varchar(50) NULL, " +
                "   DateAdded Date NULL, " +
                "   LastModified DateTime NULL, " +
                "   Notes Clob NULL, " +
                "   AmountOwed REAL NULL, " +
                "   BigInt DECIMAL(20) NULL, " +
                "   TestInstant DateTime NULL, " +
                "   SomeDate DateTime NULL, " +
                "   TestInstant2 DateTime NULL, " +
                "   WhatMiteIsIt TIME NULL, " +
                "   WhatTimeIsIt TIME NULL) ";

        executeCommand(sql, con);
    }

    public void testAnything() {
        log.info("HELLO HSQLDB!");
    }

}
