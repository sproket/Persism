package net.sf.persism;

/**
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/21/11
 * Time: 2:31 PM 
 */

import net.sf.persism.dao.DAOFactory;
import net.sf.persism.dao.Order;
import net.sf.persism.ddl.FieldDef;
import net.sf.persism.ddl.TableDef;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TestOracle extends BaseTest {

    private static final Log log = Log.getLogger(TestOracle.class);

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/oracle.properties"));

        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");

        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        con = new net.sf.log4jdbc.ConnectionSpy(con);

        createTables();

        query = new Query(con);
        command = new Command(con);
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testInsert() {
        try {

            Order order = DAOFactory.newOrder(con);
            order.setName("MOO");

            command.insert(order);
            log.info(order);
            assertTrue("order # > 0", order.getId() > 0);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    public void testTimeStamp() {
        Statement st = null;
        java.sql.ResultSet rs = null;

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
            commands.add("DROP TRIGGER BI_ORDERS");
            commands.add("DROP TABLE ORDERS");
            commands.add("DROP SEQUENCE ORDERS_SEQ");
        }

        commands.add("CREATE TABLE  \"ORDERS\" " +
                "(\"ID\" INT, " +
                "\"NAME\" VARCHAR2(50), " +
                "\"ROW__ID\" VARCHAR2(10), " +
                "\"CUSTOMER_ID\" VARCHAR(10), " +
                "\"PAID\" NUMBER(1), " +
                "\"CREATED\" DATE, " +

                " CONSTRAINT \"ORDERS_PK\" PRIMARY KEY (\"ID\") ENABLE" +
                "   ) ");

/*
                " ROW_ID VARCHAR(30) NULL, " +
                " Customer_ID VARCHAR(10) NULL, " +
                " PAID BIT NULL, " +
                " CREATED datetime " +

 */

        commands.add("CREATE SEQUENCE   \"ORDERS_SEQ\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 101 CACHE 20 NOORDER NOCYCLE");

        commands.add("CREATE trigger \"BI_ORDERS\" " +
                "  before insert on \"ORDERS\" " +
                "  for each row " +
                "begin " +
                "  if :NEW.\"ID\" is null then " +
                "    select \"ORDERS_SEQ\".nextval into :NEW.\"ID\" from dual; " +
                "  end if; " +
                "end;");


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
                " Date_Registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + // todo oracle timestamp results in a oracle specific class - FUCKEN ORACLE
                //" Date_Registered DATE DEFAULT CURRENT_TIMESTAMP, " +
                " Date_Of_Last_Order DATE " +
                ") ");


        if (UtilsForTests.isTableInDatabase("TESTTIMESTAMP", con)) {
            commands.add("DROP TABLE TESTTIMESTAMP");
        }

        commands.add("CREATE TABLE TESTTIMESTAMP ( NAME VARCHAR(10), TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP ) ");

        Statement st = null;
        try {

            for (String command : commands) {
                log.info(command);
                st = con.createStatement();
                st.executeUpdate(command);
                st.close();
//                connection.commit();
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

    public void testCreateTable() {
        Statement st = null;
        try {

            st = con.createStatement();

/*
            if (Util.isTableInDatabase("Employees", con)) {
                st.execute("DROP TABLE Employees");
            }

            String s= "CREATE TABLE Employees (\n" +
                    "Employee_ID INTEGER,\n" +
                    "Name VARCHAR(30)\n" +
                    ")";
            st.execute(s);
*/

            TableDef table = new TableDef();
            table.setName("Employees");
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


}