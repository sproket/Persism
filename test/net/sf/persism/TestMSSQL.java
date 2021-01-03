package net.sf.persism;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 6:10 AM
 */

import net.sf.persism.dao.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class TestMSSQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMSSQL.class);

    protected void setUp() throws Exception {
        super.setUp();

        //log.debug("SETUP DEBUG");
        //log.warn("SETUP WARN");
        //log.error("SETUP ERROR");
        //log.error("SETUP ERROR", new Throwable());
        // todo somehow test with JTDS and MSSQL drivers.
        Properties props = new Properties();
        //props.load(getClass().getResourceAsStream("/jtds.properties"));
        props.load(getClass().getResourceAsStream("/mssql.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);
        con = DriverManager.getConnection(url, username, password);

        // con = new net.sf.log4jdbc.ConnectionSpy(con);

        createTables();

        command = new Command(con);
        query = new Query(con);

    }

    protected void tearDown() throws Exception {
        Statement st = null;
        try {
            st = con.createStatement();
            st.execute("TRUNCATE TABLE ORDERS");
            st.execute("TRUNCATE TABLE EXAMCODE");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            UtilsForTests.cleanup(st, null);
        }

        super.tearDown();
    }

    public void testProcedure() {

        try {
            query.readList(Procedure.class, "SELECT ExamCode_No, EXAMTYPE_NO, DESC_E FROM EXAMCODE");
        } catch (PersismException e) {
            assertTrue("exception should be 'Object class net.sf.persism.dao.Procedure was not properly initialized.'", e.getMessage().startsWith("Object class net.sf.persism.dao.Procedure was not properly initialized"));
        }


        long now = System.currentTimeMillis();
        List<Procedure> list = query.readList(Procedure.class, "SELECT * FROM EXAMCODE");
        log.info("time to read procs 1: " + (System.currentTimeMillis() - now));
        log.info(list.toString());

        now = System.currentTimeMillis();
        list = query.readList(Procedure.class, "SELECT * FROM EXAMCODE");
        log.info("time to read procs 2: " + (System.currentTimeMillis() - now));

        for (Procedure procedure : list) {
            log.info(procedure.getExamCodeNo() + " " + procedure.getDescription() + " " + procedure.getModalityId());
        }
        log.info("time to display procs: " + (System.currentTimeMillis() - now));

        Procedure proc1 = new Procedure();
        proc1.setDescription("COW");
        command.insert(proc1);

        now = System.currentTimeMillis();
        list = query.readList(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 1);
        log.info("time to read procs 3: " + (System.currentTimeMillis() - now));
        assertEquals("should only have 1 proc in the list", 1, list.size());

        // todo test delete with mulitple primary keys
        Procedure proc = list.get(0);
        int result = command.delete(proc);
        assertEquals("Should be 1 for delete", 1, result);
    }

    public void testRoom() {

        //query = new Query(con);
        // todo use this kind to test a failure. This should fail because we are not initializing all room properties
        long now = System.currentTimeMillis();
        List<Room> list = query.readList(Room.class, "SELECT Room_no, Desc_E, Intervals  FROM ROOMS");
        log.info("time to read rooms: " + (System.currentTimeMillis() - now) + " size: " + list.size());

        now = System.currentTimeMillis();
        list = query.readList(Room.class, "SELECT Room_no, Desc_E, Intervals  FROM ROOMS");
        log.info("time to read rooms again: " + (System.currentTimeMillis() - now));

        now = System.currentTimeMillis();
        for (Room room : list) {
            log.info(room.getRoomNo() + " " + room.getDescription());
        }
        log.info("time to display rooms : " + (System.currentTimeMillis() - now));
    }

    public void testJunk() {
        // query = new Query(con);
        int n = getClass().getName().lastIndexOf(".");
        String className;
        if (n > -1) {
            className = getClass().getName().substring(n + 1);
        } else {
            className = getClass().getName();
        }

        log.info("|" + className + "| ---- " + getClass().getName());

        List<String> list = new ArrayList<String>(3);
        list.add("Blobbo");
        list.add("Cracked");
        list.add("Dumbo");

        // Convert a collection to Object[], which can store objects
        // of any type.
        Object[] ol = list.toArray();
        log.info("Array of Object has length " + ol.length);

        // This would throw an ArrayStoreException if the line
        // "list.add(new Date())" above were uncommented.
        String[] sl = list.toArray(new String[0]);
        log.info("Array of String has length " + sl.length + " " + Arrays.asList(sl));
    }

    public void testQuery() {

        // query = new Query(con);
        String sql;
        sql = "select top 10 ExamID, p.DESC_E ProcedureDescription, r.DESC_E RoomDescription, eXaMdAtE from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";


        List<QueryResult> list = query.readList(QueryResult.class, sql);
        log.info(list.toString());

        // Try again changing case of some fields.
        sql = "select top 10 ExamID, p.Desc_E ProcedureDescription, r.Desc_E RoomDescription, ExamDate from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";

        list = query.readList(QueryResult.class, sql);
        log.info(list.toString());

    }

    public void testAllColumnsMappedException() {
        boolean shouldHaveFailed = false;
        try {
            query.read(Contact.class, "select [identity] from Contacts");
        } catch (PersismException e) {
            shouldHaveFailed = true;
            log.warn(e.getMessage(), e);
            assertEquals("message should be ", "Object class net.sf.persism.dao.Contact was not properly initialized. Some properties not found in the queried columns. : [Company, Email, StateProvince, Address2, Lastname, PartnerID, Address1, City, Firstname, LastModified, Type, ZipPostalCode, Country, Division, DateAdded, ContactName]", e.getMessage());
        }
        assertEquals("should have failed", true, shouldHaveFailed);
    }

    public void testAdditionalPropertyNotMappedException() {
        boolean shouldHaveFailed = false;
        try {
            query.readList(ContactFail.class, "select * from Contacts");
        } catch (PersismException e) {
            shouldHaveFailed = true;
            log.warn(e.getMessage(), e);
            assertEquals("message should be ", "Object class net.sf.persism.dao.ContactFail was not properly initialized. Some properties not found in the queried columns (fail).", e.getMessage());
        }

        assertEquals("should have failed", true, shouldHaveFailed);
    }

    public void testCountQuery() {

        String sql;
        sql = "select examdate from exams";

        java.util.Date date = query.read(java.util.Date.class, sql);
        log.info("" + date);

        sql = "select count(*) from exams";
        int exams = query.read(Integer.class, sql);
        log.info("" + exams); // todo TEST COUNT > 0

        sql = "select count(*) from exams where examDate > ?";
        Date d = new Date(1997 - 1900, 2, 4);
        log.info("" + d);
        exams = query.read(Integer.class, sql, d);
        log.info("" + exams); // todo fix this test
    }


    public void testUpdate() {


        try {
            Procedure proc1; // = query.readObject(Procedure.class, "select * from examcode where examcode_no=3");
            proc1 = new Procedure();
            proc1.setDescription("new proc");
            command.insert(proc1);

            int examCodeNo = proc1.getExamCodeNo();
            assertTrue("examcode no > 0", examCodeNo > 0);


            assertTrue("should get a proc for ?" + examCodeNo, query.read(proc1));


            Procedure proc2; // = query.readObject(Procedure.class, "select * from examcode where examcode_no=?", 3);
            proc2 = new Procedure();
            proc2.setExamCodeNo(examCodeNo);
            query.read(proc2);

            // todo useless test. Probably should test that all properties match up...
            assertEquals("both procs should be the same id: 3 ", proc1.getExamCodeNo(), proc2.getExamCodeNo());

            proc1.setDescription("JUNK JUNK JUNK");

            command.update(proc1);

            //proc2 = query.readObject(Procedure.class, "select * from examcode where examcode_no=3");
            proc2.setExamCodeNo(examCodeNo);
            query.read(proc2);
            assertEquals("should be JUNK JUNK JUNK", "JUNK JUNK JUNK", proc2.getDescription());

            Order order = DAOFactory.newOrder(con);
            order.setName("MOO");
            command.insert(order);

            List<Order> orders = query.readList(Order.class, "SELECT * FROM ORDERS");
            assertEquals("size s/b 1", 1, orders.size());

            order = orders.get(0);

            order.setName("COW");

            command.update(order);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testInsert() {
        // query = new Query(con);
        Procedure procedure = new Procedure();
        procedure.setDescription("TEST 99");
        procedure.setModalityId(2);
        procedure.setSomeDate(new java.util.Date());

        command.insert(procedure);
        log.info("" + procedure);
        //asseertE
    }

    public void XtestREMOVE() {

        // query = new Query(con);
        try {
            String[] removePatterns = {"exam_techimage_backup", "interp_dximage_backup"};
            String[] types = {"TABLE"};
            java.sql.ResultSet rs = con.getMetaData().getTables(null, null, null, types);
            List<String> tablesToDrop = new ArrayList<String>(32);
            while (rs.next()) {
                String table = rs.getString("TABLE_NAME");
                if (table.contains("exam_techimage_backup") || table.contains("interp_dximage_backup")) {
                    tablesToDrop.add(table);
                }
            }
            log.info("" + tablesToDrop);

            Statement st = con.createStatement();
            for (String table : tablesToDrop) {
                st.execute("DROP TABLE " + table);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void testCreateTable() {
        try {
            command.executeSQL("DROP TABLE Contacts");
        } catch (Exception e) {
            log.warn(e);
        }
        String sql = "CREATE TABLE [dbo].[Contacts]( " +
                " [identity] [uniqueidentifier] NOT NULL, " +
                " [PartnerID] [uniqueidentifier] NULL, " +
                " [Type] [char](2) NOT NULL, " +
                " [Firstname] [nvarchar](50) NULL, " +
                " [Lastname] [nvarchar](50) NULL, " +
                " [ContactName] [nvarchar](50) NULL, " +
                " [Company] [nvarchar](50) NULL, " +
                " [Division] [nvarchar](50) NULL, " +
                " [Email] [nvarchar](50) NULL, " +
                " [Address1] [nvarchar](50) NULL, " +
                " [Address2] [nvarchar](50) NULL, " +
                " [City] [nvarchar](50) NULL, " +
                " [StateProvince] [nvarchar](50) NULL, " +
                " [ZipPostalCode] [varchar](10) NULL, " +
                " [Country] [nvarchar](50) NULL, " +
                " [DateAdded] [smalldatetime] NULL, " +
                " [LastModified] [smalldatetime] NULL " +
                " ) ";

        command.executeSQL(sql);
        /* TODO
            CONSTRAINT [PK_Contacts] PRIMARY KEY CLUSTERED
                (
                [identity] ASC
                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
            ) ON [PRIMARY]
        GO

        ALTER TABLE [dbo].[Contacts] ADD  CONSTRAINT [DF_Contacts_identity]  DEFAULT (newid()) FOR [identity]
        GO
         */

        UUID id = UUID.randomUUID();
        Contact contact = new Contact();
        contact.setIdentity(id);
        contact.setFirstname("Fred");
        contact.setLastname("Flintstone");
        contact.setDivision("DIVISION X");
        contact.setLastModified(new Date(System.currentTimeMillis() - 100000000l));
        contact.setContactName("Fred Flintstone");
        contact.setAddress1("123 Sesame Street");
        contact.setAddress2("Appt #0 (garbage can)");
        contact.setCompany("Grouch Inc");
        contact.setCountry("US");
        contact.setCity("Philly?");
        contact.setType("X");
        contact.setDateAdded(new Date(System.currentTimeMillis()));
        command.insert(contact);

        System.out.println(query.readList(Contact.class, "select * from Contacts"));

    }

    public void testDetectAutoInc() {

        // query = new Query(con);
        try {
            Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT * FROM ORDERS WHERE 1=2");
            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                log.info(rsMetaData.getColumnName(i));
                log.info(rsMetaData.isAutoIncrement(i));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    // todo test against pinf-win machine instead.
    public void testNullPointerWithTableTypes() {
//        persister = new Persister(con);
        java.sql.ResultSet rs = null;
        Statement st = null;
        try {
            // NULL POINTER WITH
            // http://social.msdn.microsoft.com/Forums/en-US/sqldataaccess/thread/5c74094a-8506-4278-ac1c-f07d1bfdb266
            String[] tableTypes = {"TABLE"};
            //st = con.createStatement();

            //rs = st.executeQuery("SELECT 1");
            //rs.close();

            rs = con.getMetaData().getTables(null, "%", null, tableTypes);
            while (rs.next()) {
                log.info(rs.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            throw new PersismException(e);

        } finally {
            Util.cleanup(st, rs);
        }

    }

    @Override
    protected void createTables() throws SQLException {
        Statement st = null;
        List<String> commands = new ArrayList<String>(3);

        if (UtilsForTests.isTableInDatabase("Orders", con)) {
            commands.add("DROP TABLE Orders");

        }
        commands.add("CREATE TABLE Orders ( " +
                " [ID] [int] IDENTITY(1,1) NOT NULL, " +
                " NAME VARCHAR(30) NULL, " +
                " ROW_ID VARCHAR(30) NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " PAID BIT NULL, " +
                " CREATED datetime " +
                ") ");


        if (UtilsForTests.isTableInDatabase("Customers", con)) {
            commands.add("DROP TABLE Customers");
        }

        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region VARCHAR(10) NULL, " +
                " Postal_Code VARCHAR(10) NULL, " +
                " Country VARCHAR(2) NULL, " +
                " Phone VARCHAR(30) NULL, " +
                " Fax VARCHAR(30) NULL, " +
                " Date_Registered datetime default current_timestamp, " +
                " Date_Of_Last_Order datetime " +
                ") ");

        if (UtilsForTests.isTableInDatabase("TABLENOPRIMARY", con)) {
            commands.add("DROP TABLE TABLENOPRIMARY");
        }

        commands.add("CREATE TABLE TABLENOPRIMARY ( " +
                " ID INT, " +
                " Name VARCHAR(30), " +
                " Field4 VARCHAR(30), " +
                " Field5 DATETIME " +
                ") ");

        try {
            st = con.createStatement();
            for (String command : commands) {
                st.execute(command);
            }

        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}