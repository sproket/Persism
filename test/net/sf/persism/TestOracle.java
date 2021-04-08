package net.sf.persism;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/21/11
 * Time: 2:31 PM
 */

import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.*;
import net.sf.persism.ddl.FieldDef;
import net.sf.persism.ddl.TableDef;
import org.junit.experimental.categories.Category;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Category(ExternalDB.class)
public final class TestOracle extends BaseTest {

    private static final Log log = Log.getLogger(TestOracle.class);

    @Override
    protected void setUp() throws Exception {
        // Turn off SQLMode for next MSSQL Test so it uses JTDS
        BaseTest.mssqlmode = false;
        connectionType = ConnectionTypes.Oracle;
        MSSQLDataSource.removeInstance();
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/oracle.properties"));

        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");

        Class.forName(driver);
        log.info(props);
//        con = DriverManager.getConnection(url); //, username, password);
        con = OracleDataSource.getInstance().getConnection();

        createTables();

        session = new Session(con);

        // Possible UUID
        // https://www.reddit.com/r/java/comments/l9gv6d/announcing_persism_100_a_no_nonsense_orm_for_java/glwk625/

        /*
CREATE USER PINF IDENTIFIED BY pinf;
grant create session to pinf;
grant create table to pinf;
alter user pinf quota unlimited on users;
grant create view, create procedure, create sequence to pinf;
grant create trigger, create sequence to pinf;
         */

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void testContactTable() throws SQLException {
        super.testContactTable();

        // TODO Oracle getting Generated GUID like postgreSQL
//        // Insert NULL GUID -- should give us back a value
//        Contact contact = new Contact();
//        contact.setFirstname("Fred");
//        contact.setLastname("Flintstone");
//        contact.setDivision("DIVISION X");
//        contact.setLastModified(new Timestamp(System.currentTimeMillis() - 100000000l));
//        contact.setContactName("Fred Flintstone");
//        contact.setAddress1("123 Sesame Street");
//        contact.setAddress2("Appt #0 (garbage can)");
//        contact.setCompany("Grouch Inc");
//        contact.setCountry("US");
//        contact.setCity("Philly?");
//        contact.setType("X");
//        contact.setDateAdded(new java.sql.Date(System.currentTimeMillis()));
//        contact.setAmountOwed(100.23f);
//        contact.setNotes("B:AH B:AH VBLAH\r\n BLAH BLAY!");
//        contact.setWhatTimeIsIt(Time.valueOf(LocalTime.now()));
//        session.insert(contact);
//
//        log.info("contact after insert: " + contact);
//        assertNotNull("should not be null identity", contact.getIdentity());
//
//        session.fetch(contact);
//
//        contact.setDivision("DIVISION Y");
//        session.update(contact);
//
//        session.delete(contact);
    }

/*
YOU NEED THE TRIGGER PART
CREATE table "TEST" (
    "ID"         NUMBER(10) NOT NULL,
    "NAME"       VARCHAR2(20),
    constraint  "TEST_PK" primary key ("ID")
)
/

CREATE sequence "TEST_SEQ"
/

CREATE trigger "BI_TEST"
  before insert on "TEST"
  for each row
begin
  if :NEW."ID" is null then
    select "TEST_SEQ".nextval into :NEW."ID" from dual;
  end if;
end;
/
*/


    @Override
    protected void createTables() throws SQLException {

        List<String> commands = new ArrayList<String>(4);

        if (UtilsForTests.isTableInDatabase("ORDERS", con)) {
            try {
                executeCommand("DROP TRIGGER BI_ORDERS", con);
            } catch (SQLException e) {
                log.info(e.getMessage());
            }
            executeCommand("DROP TABLE ORDERS", con);
            try {
                executeCommand("DROP SEQUENCE ORDERS_SEQ", con);
            } catch (SQLException e) {
                log.info(e.getMessage());
            }

        }

        if (UtilsForTests.isTableInDatabase("ORACLEBIT", con)) {
            commands.add("DROP TABLE ORACLEBIT");
        }

        commands.add("CREATE TABLE  \"ORACLEBIT\" " +
                "(\"ID\" INT, " +
                "\"NAME\" VARCHAR2(50), " +
                "\"ROW__ID\" VARCHAR2(10), " +
                "\"CUSTOMER_ID\" VARCHAR(10), " +
                "\"PAID\" NUMBER(3), " + // BIT TEST
                "\"CREATED\" DATE, " +
                "\"GARBAGE\" CHAR(1), " + // BIT TEST
                "\"BIGGIE\" NUMBER(38), " + // BIT TEST
                " CONSTRAINT \"ORACLEBIT_PK\" PRIMARY KEY (\"ID\") ENABLE" +
                "   ) ");


        // BIT TYPE
        // https://stackoverflow.com/questions/2426145/oracles-lack-of-a-bit-datatype-for-table-columns#2427016
        // REAL AUTO-INC
        // https://www.databasestar.com/auto_increment-oracle-sql/
        commands.add("CREATE TABLE  \"ORDERS\" " +
//                "(\"ID\" NUMBER, " +
                "(\"ID\" NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY, " +
                "\"NAME\" VARCHAR2(50), " +
                "\"ROW__ID\" VARCHAR2(10), " +
                "\"CUSTOMER_ID\" VARCHAR(10), " +
                "\"PAID\" NUMBER(3), " +
                " Prepaid CHAR(1) NULL," +
                " IsCollect NUMBER(1) NULL," +
                " IsCancelled NUMBER(3) NULL," +
                "\"CREATED\" DATE DEFAULT CURRENT_TIMESTAMP, " +
                "\"DATE_PAID\" DATE, " +
                "\"DATE_SOMETHING\" DATE, " +
                "\"BIT1\" CHAR(1), " + // BIT TEST
                "\"BIT2\" NUMBER(3), " + // BIT TEST
                " CONSTRAINT \"ORDERS_PK\" PRIMARY KEY (\"ID\") ENABLE" +
                "   ) ");

//        commands.add("CREATE SEQUENCE   \"ORDERS_SEQ\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 101 CACHE 20 NOORDER NOCYCLE");
//
//        commands.add("CREATE trigger \"BI_ORDERS\" " +
//                "  before insert on \"ORDERS\" " +
//                "  for each row " +
//                "begin " +
//                "  if :NEW.\"ID\" is null then " +
//                "    select \"ORDERS_SEQ\".nextval into :NEW.\"ID\" from dual; " +
//                "  end if; " +
//                "end;");
//

        if (UtilsForTests.isTableInDatabase("CUSTOMERS", con)) {
            commands.add("DROP TABLE CUSTOMERS");
        }

        commands.add("CREATE TABLE CUSTOMERS ( " +
                " Customer_ID VARCHAR(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) DEFAULT 'US' NOT NULL, " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Date_Registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                " SomeDouble NUMBER(38,2) NULL," +
                " SomeInt NUMBER(38,2) NULL," +
                " STATUS CHAR(1) NULL, " +
                " Date_Of_Last_Order DATE, " +
                " TestLocalDate DATE, " +
                " TestLocalDateTime TIMESTAMP " +
                ") ");


        if (UtilsForTests.isTableInDatabase("TESTTIMESTAMP", con)) {
            commands.add("DROP TABLE TESTTIMESTAMP");
        }

        commands.add("CREATE TABLE TESTTIMESTAMP ( NAME VARCHAR(10), TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP ) ");
        executeCommands(commands, con);

        if (UtilsForTests.isTableInDatabase("CONTACTS", con)) {
            executeCommand("DROP TABLE CONTACTS", con);
        }

        String sql = "CREATE TABLE Contacts( " +
                "   identity RAW(16) default sys_guid() NOT NULL PRIMARY KEY, " +  // test binary(16)
                "   PartnerID VARCHAR(36) NULL, " + // test varchar(36) allow null for test
                "   Type CHAR(2) NOT NULL, " +
                "   Firstname VARCHAR(50) NOT NULL, " +
                "   Lastname VARCHAR(50) NOT NULL, " +
                "   ContactName VARCHAR(50) NOT NULL, " +
                "   Company VARCHAR(50) NOT NULL, " +
                "   Division VARCHAR(50) NULL, " +
                "   Email VARCHAR(50) NULL, " +
                "   Address1 VARCHAR(50) NULL, " +
                "   Address2 VARCHAR(50) NULL, " +
                "   City VARCHAR(50) NULL, " +
                "   StateProvince VARCHAR(50) NULL, " +
                "   ZipPostalCode VARCHAR(10) NULL, " +
                "   Country VARCHAR(50) NULL, " +
                "   DateAdded DATE NULL,  " +
                "   LastModified TIMESTAMP NULL, " +
                "   Notes CLOB NULL, " +
                "   Status NUMBER(3), " +
                "   AmountOwed  NUMBER(10,2) NULL, " +
                "   BigInt  NUMBER(20) NULL, " +
                "   SomE_DaTE TIMESTAMP NULL, " +
                "   TestInstant TIMESTAMP NULL, " +
                "   TestInstant2 DATE NULL, " +
                "   WhatMiteIsIt TIMESTAMP NULL, " +
                "   WhatTimeIsIt TIMESTAMP NULL " +
                " ) ";
        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("Invoices", con)) {
            executeCommand("DROP TABLE Invoices", con);
        }
        sql = "CREATE TABLE Invoices (" +
                " \"INVOICE_ID\" NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY, " +
                " \"CUSTOMER_ID\" varchar(10) NOT NULL, " +
                " \"Paid\" CHAR(1) NOT NULL, " +
                " \"Price\" NUMERIC(7,3) NOT NULL, " +
                " \"ActualPrice\" NUMERIC(7,3) NOT NULL, " +
                " \"Status\" INT DEFAULT 1, " +
                " \"Created\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + // make read-only in Invoice Object
                " \"Quantity\" NUMERIC(10) NOT NULL, " +
//                " \"Total\" NUMERIC(10,3) NOT NULL, " +
                " \"Discount\" NUMERIC(10,3) NOT NULL, " +
                " CONSTRAINT \"Invoices_PK\" PRIMARY KEY (\"INVOICE_ID\") ENABLE" +
                "   ) ";
        executeCommand(sql, con);

        // ORACLE DOESNT have TIME so we use TIMESTAMP FOR IT
        if (UtilsForTests.isTableInDatabase("DateTestLocalTypes", con)) {
            executeCommand("DROP TABLE DateTestLocalTypes", con);
        }

        sql = "CREATE TABLE DateTestLocalTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIMESTAMP," +
                " DateAndTime TIMESTAMP) ";

        executeCommand(sql, con);

        if (UtilsForTests.isTableInDatabase("DateTestSQLTypes", con)) {
            executeCommand("DROP TABLE DateTestSQLTypes", con);
        }

        sql = "CREATE TABLE DateTestSQLTypes ( " +
                " ID INT, " +
                " Description VARCHAR(100), " +
                " DateOnly DATE, " +
                " TimeOnly TIMESTAMP," +
                " UtilDateAndTime TIMESTAMP," +
                " DateAndTime TIMESTAMP) ";

        executeCommand(sql, con);


    }


    public void testInsert() throws Exception {
        OracleOrder order = (OracleOrder) DAOFactory.newOrder(con);
        order.setName("MOO");
        order.setPaid(true);
        order.setBit2("ANYTHING?");
        order.setBit1(new BigDecimal(0));
        try {
            session.insert(order);
        } catch (PersismException e) {
            log.info("ANYTHING? " + e.getMessage());
            // net.sf.persism.PersismException: ORA-01722: invalid number
            // Anything is not a number. Really?
            // Changed this to handle inside Persism. So we now get NumberFormatException rather than passing it to the DB to error out.
            assertNotNull("message should not be null?", e.getMessage());
            //assertTrue("should contain invalid number", e.getMessage().contains("invalid number"));
            assertTrue("should contain NumberFormatException", e.getMessage().contains("NumberFormatException"));
        }

        order.setBit2("1");
        session.insert(order);
        log.info("inserted? " + order);
        assertTrue("order # > 0", order.getId() > 0);

        order = (OracleOrder) DAOFactory.newOrder(con);
        order.setName("MOO2");
        order.setPaid(false);
        session.insert(order);

        order = (OracleOrder) DAOFactory.newOrder(con);
        order.setName("MOO3");
        session.insert(order);

        List<Order> list = session.query(Order.class, "select * from ORDERS");
        log.info("List of orders:" + list);

    }

    public void testTimeStamp() {
        Statement st = null;
        ResultSet rs = null;

        // TESTTIMESTAMP
        try {
            st = con.createStatement();
            st.executeUpdate("INSERT INTO TESTTIMESTAMP (NAME) VALUES ('TEST')");

            rs = st.executeQuery("SELECT * FROM TESTTIMESTAMP");
            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                log.info("testTimeStamp: TYPE: " + rsmd.getColumnType(2) + " " + Types.convert(rsmd.getColumnType(2))); // second column
                Date dt = rs.getDate("TS"); // loses time component
                Object obj = rs.getObject("TS"); // returns fucken oracle.sql.TIMESTAMP class
                Timestamp ts = rs.getTimestamp("TS");

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                log.info("testTimeStamp: " + format.format(dt));
                log.info("testTimeStamp: " + format.format(ts));
                log.info("testTimeStamp: " + obj + " " + obj.getClass().getName());
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            UtilsForTests.cleanup(st, rs);
        }
    }


    public void testBits() {
        OracleBit bt1 = new OracleBit();
        bt1.setId(1);
        bt1.setName("1");
        bt1.setCustomerId("CUST ID");
        bt1.setCreated(new Date(System.currentTimeMillis()));
        bt1.setPaid(true);
        bt1.setGarbage(true);
        bt1.setBiggie(new BigInteger("8972987349823740948742094874092478", 10));
        session.insert(bt1);

        OracleBit test = new OracleBit();
        test.setId(1);

        assertTrue(session.fetch(test));
        log.info(test);
        assertTrue(test.isPaid());
        assertTrue(test.isGarbage());
        assertEquals("s/b '8972987349823740948742094874092478'", "8972987349823740948742094874092478", test.getBiggie().toString());

        test = new OracleBit();
        test.setId(2);
        test.setName("2");
        test.setCustomerId("CUST ID");
        test.setCreated(new Date(System.currentTimeMillis()));
        test.setPaid(false);
        test.setGarbage(false);

        session.insert(test);

        assertTrue(session.fetch(test));
        log.info(test);
        assertFalse(test.isPaid());
        assertFalse(test.isGarbage());

        test = new OracleBit();
        test.setId(3);
        test.setName("3");
        test.setCustomerId("CUST ID");
        test.setCreated(new Date(System.currentTimeMillis()));

        session.insert(test);

        assertTrue(session.fetch(test));
        log.info(test);
        assertNull(test.isPaid());
        assertNull(test.isGarbage());

        session.query(OracleBit.class, "select * from OracleBit");

        // count 3 after from query
    }

    public void testCreateTableFromTableDef() {
        Statement st = null;
        try {
            st = con.createStatement();
            TableDef table = new TableDef();
            table.setName("PINFEmployees");
            table.addField(new FieldDef("ID", Integer.class, 10, 0));
            table.addField(new FieldDef("Name", String.class, 50, 0));
            table.addField(new FieldDef("HireDate", Date.class));
            table.addField(new FieldDef("Salary1", BigDecimal.class, 20, 3));
            table.addField(new FieldDef("Salary2", Double.class, 14, 3));
            table.addField(new FieldDef("Salary3", Float.class, 9, 3));

            UtilsForTests.createTable(table, con);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            UtilsForTests.cleanup(st, null);
        }
    }

    public void testGetTriggerMetaData() throws SQLException {

        // https://stackoverflow.com/questions/30074300/how-to-get-all-trigger-names-from-a-database-using-java-jdbc
        DatabaseMetaData dbmd = con.getMetaData();

        System.out.println("**********");
        System.out.println("TABLES:");
        System.out.println("**********");

        ResultSet result = dbmd.getTables("%", "%", "ORDERS", new String[]{"TABLE"});
        while (result.next()) {
            String tableName = result.getString("TABLE_NAME");
            String catalog = result.getString("TABLE_CAT");
            String schema = result.getString("TABLE_SCHEM");
            System.out.println("Table: " + tableName);

            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                System.out.println(i + " - " + result.getMetaData().getColumnLabel(i) + " = " + result.getObject(i));
            }

            System.out.println("PRIMARY KEYS:");
            try (ResultSet primaryKeys = dbmd.getPrimaryKeys(catalog, schema, tableName)) {
                while (primaryKeys.next()) {
                    System.out.println("Primary key: " + primaryKeys.getString("COLUMN_NAME"));
                    for (int j = 1; j < primaryKeys.getMetaData().getColumnCount(); j++) {
                        System.out.println(j + " - " + primaryKeys.getMetaData().getColumnLabel(j) + " = " + primaryKeys.getObject(j));
                    }
                }
            }

            System.out.println("---------------------");
        }


        System.out.println("**********");
        System.out.println("TRIGGERS:");
        System.out.println("**********");
        result = dbmd.getTables("%", "%", "%ORDERS", new String[]{"TRIGGER"});
        while (result.next()) {
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                System.out.println(i + " - " + result.getMetaData().getColumnLabel(i) + " = " + result.getObject(i));
            }
            System.out.println("---------------------");
        }

        Statement st = con.createStatement();
        ResultSet ut = st.executeQuery("SELECT * FROM USER_TRIGGERS WHERE TRIGGER_NAME='BI_ORDERS'");
        while (ut.next()) {
            for (int i = 1; i <= ut.getMetaData().getColumnCount(); i++) {
                System.out.println(i + " - " + ut.getMetaData().getColumnLabel(i) + " = " + ut.getObject(i));
            }
        }
//        System.out.println("**********");
//        System.out.println("SEQUENCES:");
//        System.out.println("**********");
//        result = dbmd.getTables("%", "%", "ORDERS", new String[]{"SEQUENCE"});
//        while (result.next()) {
//            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
//                System.out.println(i + " - " + result.getMetaData().getColumnLabel(i) + " = " + result.getObject(i));
//            }
//            System.out.println("---------------------");
//        }
//
//

////        String sql = "SELECT * FROM sys.trigger$";
//
//String sql = "select " +
//        "   dbms_metadata.GET_DDL(u.object_type,u.object_name,'PINF') " +
//        "from " +
//        "   dba_objects u " +
//        "where " +
//        "   owner = 'PINF'";
//        System.out.println(sql);
//        try (Statement st = con.createStatement()) {
//            result = st.executeQuery(sql);
//
//            while (result.next()) {
//                for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
//                    System.out.println(i + " - " + result.getMetaData().getColumnLabel(i) + " = " + result.getObject(i));
//                }
//                System.out.println("---------------------");
//            }
//        }
    }

    @Override
    public void testAllDates() {
        super.testAllDates();
    }

    @Override
    public void testGetDbMetaData() throws SQLException {
        // TODO SUPER SLOW ON NEW ORACLE FWR
        //super.testGetDbMetaData();
    }
}