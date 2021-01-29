package net.sf.persism;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 6:10 AM
 */

import net.sf.persism.dao.*;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

public class TestMSSQL extends BaseTest {

    private static final Log log = Log.getLogger(TestMSSQL.class);

    protected void setUp() throws Exception {
        // BaseTest.mssqlmode = false; // to run in JTDS MODE
        super.setUp();
        log.error("SQLMODE? " + BaseTest.mssqlmode);
        con = MSSQLDataSource.getInstance().getConnection();
        log.info("PRODUCT? " + con.getMetaData().getDriverName() + " - " + con.getMetaData().getDriverVersion());

        createTables();

        session = new Session(con);
    }


    protected void tearDown() throws Exception {
        Statement st = null;
        st = con.createStatement();
        try {
            st.execute("TRUNCATE TABLE Orders");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        try {
            st.execute("TRUNCATE TABLE Customers");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        try {
            st.execute("TRUNCATE TABLE EXAMCODE");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            UtilsForTests.cleanup(st, null);
        }

        // https://www.baeldung.com/java-size-of-object#:~:text=Objects%2C%20References%20and%20Wrapper%20Classes,a%20multiple%20of%204%20bytes.
        // https://stackoverflow.com/questions/52353/in-java-what-is-the-best-way-to-determine-the-size-of-an-object
        // log.warn("SESSION: " + InstrumentationAgent.getObjectSize(session));
        super.tearDown();
    }

    public void testProcedure() throws SQLException {

        Procedure procx = new Procedure();
        procx.setDescription("COW2");
        session.insert(procx);

        boolean fail = false;
        try {
            con.setAutoCommit(false);
            session.query(Procedure.class, "SELECT ExamCode_No, EXAMTYPE_NO, DESC_E FROM EXAMCODE");
        } catch (PersismException e) {
            fail = true;
            assertTrue("exception should be 'Object class net.sf.persism.dao.Procedure was not properly initialized.'", e.getMessage().startsWith("Object class net.sf.persism.dao.Procedure was not properly initialized"));
            con.setAutoCommit(true);
        }
        assertTrue(fail);

        long now = System.currentTimeMillis();
        List<Procedure> list = session.query(Procedure.class, "SELECT * FROM EXAMCODE");
        log.info("time to read procs 1: " + (System.currentTimeMillis() - now));
        log.info(list.toString());

        now = System.currentTimeMillis();
        list = session.query(Procedure.class, "SELECT * FROM EXAMCODE");
        log.info("time to read procs 2: " + (System.currentTimeMillis() - now));

        for (Procedure procedure : list) {
            log.info(procedure.getExamCodeNo() + " " + procedure.getDescription() + " " + procedure.getModalityId());
        }

        log.info("time to display procs: " + (System.currentTimeMillis() - now));

        Procedure proc1 = new Procedure();
        proc1.setDescription("COW");
        session.insert(proc1);

        // Instantiate a new and do an update
        Procedure procedure = new Procedure();
        procedure.setExamCodeNo(proc1.getExamCodeNo());
        procedure.setDescription(proc1.getDescription());
        session.update(procedure);

        session.fetch(procedure);
        procedure.setDescription(null);
        session.update(procedure);
        session.fetch(procedure);
        log.warn("NULL? " + procedure.getDescription());


        assertTrue("proc1 should have an id? ", proc1.getExamCodeNo() > 0);

        // test fetch
        Procedure proc2 = session.fetch(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 2);
        assertNotNull("proc2 should be found ", proc2);

        Procedure proc3 = session.fetch(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", -99);
        assertNull("proc3 should NOT be found ", proc3);

        Procedure proc4 = session.fetch(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 2);
        assertNotNull("proc4 should be found ", proc4);


        now = System.currentTimeMillis();
        list = session.query(Procedure.class, "select * from EXAMCODE WHERE ExamCode_No=?", 2);
        log.info("time to read procs 3: " + (System.currentTimeMillis() - now));
        assertEquals("should only have 1 proc in the list", 1, list.size());

        Procedure proc = list.get(0);
        int result = session.delete(proc);
        assertEquals("Should be 1 for delete", 1, result);
    }

    public void testRoom() {
        Room roomX = new Room();
        roomX.setDescription("room 1");
        roomX.setIntervals(new BigDecimal(10));
        roomX.setWeird("werid?");
        roomX.setJunk("junk");
        session.insert(roomX);

        long now = System.currentTimeMillis();
        List<Room> list = session.query(Room.class, "SELECT Room_no, Desc_E, Intervals, [Weird$#@]  FROM ROOMS");
        log.info("time to read rooms: " + (System.currentTimeMillis() - now) + " size: " + list.size());

        now = System.currentTimeMillis();
        list = session.query(Room.class, "SELECT *  FROM ROOMS"); // Room_no, Desc_E, Intervals
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

    public void testOutsideEnum() throws Exception {
        String sql = "INSERT INTO [Customers] ([Customer_ID], [Company_Name], [Contact_Name], [Contact_Title], " +
                "[Address], [City], [Region], [Postal_Code], [Country], [Phone], " +
                "[Fax], [STATUS], [Date_Of_Last_Order]) VALUES ( ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? ,  ? )";

        session.execute(sql, "X", "Name", "Contact", "Title", "Address", "City", "NOTAREGION", "CODe", "CA", "1", "2", "3", null);

        boolean failed = false;
        try {
            Customer c1 = new Customer();
            c1.setCustomerId("X");
            session.fetch(c1);

        } catch (Exception e) {
            failed = true;
            assertEquals("message s/b 'argument type mismatch Object class net.sf.persism.dao.Customer. Column: Region Type of property: class net.sf.persism.dao.Regions - Type read: class java.lang.String VALUE: NOTAREGION'",
                    "argument type mismatch Object class net.sf.persism.dao.Customer. Column: Region Type of property: class net.sf.persism.dao.Regions - Type read: class java.lang.String VALUE: NOTAREGION",
                    e.getMessage());
        }
        assertTrue(failed);

    }

    public void testStoredProc() throws SQLException {

        Customer c1 = new Customer();
        c1.setCustomerId("123");
        c1.setCompanyName("ABC INC");
        c1.setRegion(Regions.East);
        session.insert(c1);

        Customer cx = new Customer();
        cx.setCustomerId("123");
        cx.setCompanyName("ABC INC");
        cx.setRegion(Regions.East);
        cx.setAddress("asasasas");
        cx.setStatus('e');
        session.update(cx);

        assertNotNull("Should be defaulted", c1.getDateRegistered());

        Customer c2 = new Customer();
        c2.setCustomerId("456");
        c2.setCompanyName("XYZ INC");
        session.insert(c2);

        Order order;
        order = DAOFactory.newOrder(con);
        order.setCustomerId("123");
        order.setName("ORDER 1");
        order.setCreated(new java.sql.Date(System.currentTimeMillis()));
        order.setPaid(true);
        session.insert(order);

        order = DAOFactory.newOrder(con);
        order.setCustomerId("123");
        order.setName("ORDER 2");
        order.setCreated(new java.sql.Date(System.currentTimeMillis()));
        order.setPaid(false);
        session.insert(order);

        List<CustomerOrder> list = session.query(CustomerOrder.class, "[spCustomerOrders](?)", "123");
        log.info(list);
        // Both forms should work - the 1st is a cleaner way but this should be supported
        list = session.query(CustomerOrder.class, "{call [spCustomerOrders](?) }", "123");
        log.info(list);

        // query orders by date
        //DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String created = df.format(order.getCreated());
        List<Order> orders = session.query(Order.class, "select * from Orders where CONVERT(varchar, created, 112) = ?", created);

        log.info("ORDERS?" + orders);
    }

    public void testQuery() {

        String sql;
        sql = "select top 10 ExamID, p.DESC_E ProcedureDescription, r.DESC_E RoomDescription, eXaMdAtE from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";


        List<QueryResult> list = session.query(QueryResult.class, sql);
        log.info(list.toString());

        // Try again changing case of some fields.
        sql = "select top 10 ExamID, p.Desc_E ProcedureDescription, r.Desc_E RoomDescription, ExamDate from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";

        list = session.query(QueryResult.class, sql);
        list.stream().count();
        log.info(list.toString());

        List<Integer> simpleList;
        sql = "select top 10 ExamID from exams " +
                "left join EXAMCODE as p ON Exams.ExamCode_No = p.ExamCode_No " +
                "left join ROOMS as r ON Exams.Room_No = r.Room_No ";

        simpleList = session.query(Integer.class, sql);
        log.info(simpleList.toString());
    }

    public void testAllColumnsMappedException() {
        boolean shouldHaveFailed = false;
        try {
            session.fetch(Contact.class, "select [identity] from Contacts");
        } catch (PersismException e) {
            shouldHaveFailed = true;
            log.warn(e.getMessage(), e);
            // Apparently we don't always get the same column order?
//            assertEquals("message should be ", "Object class net.sf.persism.dao.Contact was not properly initialized. Some properties not found in the queried columns. : [Company, Email, StateProvince, Address2, Lastname, PartnerID, Address1, City, Firstname, LastModified, Type, ZipPostalCode, Country, Division, DateAdded, ContactName]", e.getMessage());
        }
        assertEquals("should have failed", true, shouldHaveFailed);
    }

    public void testAdditionalPropertyNotMappedException() {
        boolean shouldHaveFailed = false;
        try {
            session.fetch(ContactFail.class, "select * from Contacts");
        } catch (PersismException e) {
            shouldHaveFailed = true;
            log.warn(e.getMessage(), e);
            assertEquals("message should be ", "Object class net.sf.persism.dao.ContactFail was not properly initialized. Some properties not initialized in the queried columns (fail).", e.getMessage());
        }

        assertEquals("should have failed", true, shouldHaveFailed);
    }

    public void testCountQuery() {

        String sql;
        sql = "select examdate from exams";

        java.util.Date date = session.fetch(java.util.Date.class, sql);
        log.info("" + date);

        sql = "select count(*) from exams";
        int exams = session.fetch(Integer.class, sql);
        log.info("" + exams);
        assertTrue("should be > 0", exams > 0);

        sql = "select count(*) from exams where examDate > ?";
        Date d = new Date(1997 - 1900, 2, 4);
        log.info("" + d);
        exams = session.fetch(Integer.class, sql, d);
        log.info("" + exams);
        assertTrue("should be > 0", exams > 0);

    }


    public void testUpdate() {


        try {
            Procedure proc1; // = query.readObject(Procedure.class, "select * from examcode where examcode_no=3");
            proc1 = new Procedure();
            proc1.setDescription("new proc LDKJH DLKJH SLKJH DLSJKH DLSKJHD LSKDJH DSLKJH DSLKJH SLKJHD LKSJHD LKSHJD LSKJDH LSKDJH DSLKJHD SLKJDH SLDKJH SDLKJHD SLKDHS LKDJHSDLKJSDH LDKJH ");
            session.insert(proc1);

            int examCodeNo = proc1.getExamCodeNo();
            assertTrue("examcode no > 0", examCodeNo > 0);


            assertTrue("should get a proc for ?" + examCodeNo, session.fetch(proc1));


            Procedure proc2; // = query.readObject(Procedure.class, "select * from examcode where examcode_no=?", 3);
            proc2 = new Procedure();
            proc2.setExamCodeNo(examCodeNo);
            session.fetch(proc2);

            assertEquals("both procs should be the same id: 3 ", proc1.getExamCodeNo(), proc2.getExamCodeNo());

            proc1.setDescription("JUNK JUNK JUNK");

            session.update(proc1);


            //proc2 = query.readObject(Procedure.class, "select * from examcode where examcode_no=3");
            proc2.setExamCodeNo(examCodeNo);
            session.fetch(proc2);
            assertEquals("should be JUNK JUNK JUNK", "JUNK JUNK JUNK", proc2.getDescription());

            Order order = DAOFactory.newOrder(con);
            order.setName("MOO");
            session.insert(order);

            List<Order> orders = session.query(Order.class, "SELECT * FROM ORDERS");
            assertEquals("size s/b 1", 1, orders.size());

            order = orders.get(0);

            order.setName("COW");

            session.update(order);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testInsert() {
        // query = new Query(con);
        Procedure procedure = new Procedure();
        procedure.setDescription("TEST 99 LKJHDSLKJSDH LKSDJH LDSKJHD LSKDJH DSLKJHDLSKHD SLKDJHS KLDJHS DKLDH SLKDJH SDLKJHD SLKDJSHD LKSJH SLKDH SLDKJSH DLKJSH DLKSJDH LKSJH DLKSJH D");
        procedure.setModalityId(2);
        procedure.setSomeDate(new java.util.Date());

        session.insert(procedure);
        log.info("" + procedure);
        //asseertE
    }


    public void testContactTable() throws SQLException {
        try {
            session.execute("ALTER TABLE [Contacts] DROP CONSTRAINT [DF_Contacts_identity]");
        } catch (Exception e) {
            log.warn(e);
        }
        try {
            session.execute("ALTER TABLE [Contacts] DROP CONSTRAINT [PK_Contacts]");
        } catch (Exception e) {
            log.warn(e);
        }
        try {
            session.execute("DROP TABLE Contacts");
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
                " [LastModified] [smalldatetime] NULL, " +
                " [Notes] [text] NULL, " +
                " [AmountOwed] [float] NULL, " +
                " [WhatTimeIsIt] [time](7) NULL, " +
                "CONSTRAINT [PK_Contacts] PRIMARY KEY CLUSTERED " +
                "  (" +
                "   [identity] ASC " +
                "  ) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY] " +
                ") ON [PRIMARY] ";

        session.execute(sql);

        sql = "ALTER TABLE [dbo].[Contacts] ADD  CONSTRAINT [DF_Contacts_identity]  DEFAULT (newid()) FOR [identity]";
        session.execute(sql);

        // Insert specify GUID
        Contact contact = new Contact();
        contact.setIdentity(UUID.randomUUID());
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
        contact.setAmountOwed(100.23f);
        contact.setNotes("B:AH B:AH VBLAH\r\n BLAH BLAY!");
        contact.setWhatTimeIsIt(Time.valueOf(LocalTime.now()));
        session.insert(contact);

        log.info("contact after insert: " + contact);

        // Do this again to test that setting a change which doesn't actually change will throw the NoChangesDetectedForUpdateException (internally)
        // We should see this in the logs "No properties changed. No update required for Object"
        session.fetch(contact);
        contact.setDivision("DIVISION X");
        session.update(contact);


        // This case will not work.
        Contact barney = new Contact();
        //contact.setIdentity(UUID.randomUUID());
        barney.setFirstname("Barney");
        barney.setLastname("Rubble");
        barney.setDivision("DIVISION X");
        barney.setLastModified(new Date(System.currentTimeMillis() - 100000000l));
        barney.setContactName("Fred Flintstone");
        barney.setAddress1("123 Sesame Street");
        barney.setAddress2("Appt #0 (garbage can)");
        barney.setCompany("Grouch Inc");
        barney.setCountry("US");
        barney.setCity("Philly?");
        barney.setType("X");
        barney.setDateAdded(new Date(System.currentTimeMillis()));
        barney.setAmountOwed(100.23f);
        barney.setNotes("B:AH B:AH VBLAH\r\n BLAH BLAY!");

        int count = session.query(Contact.class, "select * from Contacts").size();

        boolean failed = false;
        try {
            session.insert(barney);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.'",
                    "Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.",
                    e.getMessage());
        }
        assertTrue(failed);

        assertEquals("should have same count in Contacts",
                count,
                session.query(Contact.class, "select * from Contacts").size());

        log.info(session.query(Contact.class, "select * from Contacts"));

//        tryInsertReturnall();
    }

    private void tryInsertReturnall() throws SQLException {
        // this was a test to see if I could prepare a statement and return all colums. Nope.....

        // ensure metadata is there
        log.info(session.query(Contact.class, "select * from Contacts"));

        String insertStatement = "INSERT INTO Contacts (FirstName, LastName) VALUES ( ?, ? ) ";

        PreparedStatement st = null;
        ResultSet rs = null;

        Map<String, ColumnInfo> columns = session.getMetaData().getColumns(Contact.class, con);

        String[] columnNames = columns.keySet().toArray(new String[0]);
        int x = Statement.RETURN_GENERATED_KEYS;
        st = con.prepareStatement(insertStatement, columnNames);

        st.setString(1, "Wilma");
        st.setString(1, "Flintstone");

        int ret = st.executeUpdate();
        log.info("rows insetred " + ret);
        rs = st.getGeneratedKeys();
        while (rs.next()) {
            log.info("NOPE: " + rs.getObject(1));
        }


    }

    public void testDetectAutoInc() {

        DumbTableStringAutoInc dumb = new DumbTableStringAutoInc();
        session.insert(dumb);

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

    public void testDateTimeOffset() {
        /*
        Notes:
            JTDS 1.2.5, 1.3.1 reads value as TimeStamp SQL TYPE 93
            MSSQL Driver reads reads value as -155 Not defined in java.sql.Types !
            See our Types class - we'll just check for that and set it as a TimeStamp to be consistent with JTDS

            To convert back see.
            https://stackoverflow.com/questions/36405320/using-the-datetimeoffset-datatype-with-jtds
         */
        try {


            User user = new User();
            user.setDepartment(2);
            user.setName("test 1");
            user.setTypeOfUser("X");
            user.setStatus("Z");
            user.setUserName("ABC");

            session.insert(user);

            log.info(user);

//            user = new User();
//            user.setDepartment(2);
//            user.setName("test 2");
//            user.setTypeOfUser("X");
//            user.setStatus("Z");
//            user.setUserName("XYZ");
//            user.setSomeDate();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }


    public void testNullPointerWithTableTypes() throws SQLException {
        ResultSet rs = null;
        // NULL POINTER WITH
        // http://social.msdn.microsoft.com/Forums/en-US/sqldataaccess/thread/5c74094a-8506-4278-ac1c-f07d1bfdb266
        String[] tableTypes = {"TABLE"};
        rs = con.getMetaData().getTables(null, "%", null, tableTypes);
        while (rs.next()) {
            log.info(rs.getString("TABLE_NAME"));
        }
    }

    @Override
    protected void createTables() throws SQLException {
        log.info("createTables");
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
                " STATUS CHAR(1) NULL, " +
                " CREATED datetime " +
                ") ");

        commands.add("ALTER TABLE [dbo].[Orders] ADD  CONSTRAINT [DF_Orders_CREATED]  DEFAULT (getdate()) FOR [CREATED]");

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
                " STATUS CHAR(1) NULL, " +
                " Date_Registered datetime  default current_timestamp, " +
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

        if (UtilsForTests.isTableInDatabase("DumbTableStringAutoInc", con)) {
            commands.add("DROP TABLE DumbTableStringAutoInc");
        }

        commands.add("CREATE TABLE DumbTableStringAutoInc ( " +
                " ID VARCHAR(10) )");

        executeCommands(commands, con);
    }


}