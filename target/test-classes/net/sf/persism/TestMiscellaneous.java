package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.pubs.Author;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.MSSQLServerContainer;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

@Category(TestContainerDB.class)
public class TestMiscellaneous extends TestCase {

    private static final Log log = Log.getLogger(TestMiscellaneous.class);

    @ClassRule
    private static final MSSQLServerContainer<?> DB_CONTAINER = new MSSQLServerContainer <>("mcr.microsoft.com/mssql/server:2017-latest")
            .acceptLicense();

    protected void setUp() throws Exception {

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAutoClosable() throws Exception {
        boolean mustCreateTables = false;
        if(!DB_CONTAINER.isRunning()) {
            //there are lots of warnings while this container starts, but it works.
            //it is an open issue: https://github.com/testcontainers/testcontainers-java/issues/3079
            DB_CONTAINER.start();
            mustCreateTables = true;
        }

        Class.forName(DB_CONTAINER.getDriverClassName());

        Connection con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());

        if(mustCreateTables) {
            createTables(con);
        } else{
            BaseTest.executeCommand("USE PUBS", con);
        }

        try (Session session = new Session(con)) {
            List<Author> list = session.query(Author.class, "select * from authors");
            list.stream().forEach(c -> log.info(c));
        }
        assertTrue(con.isClosed());
    }

    private void createTables(Connection con) throws SQLException {
        //from https://github.com/Microsoft/sql-server-samples/tree/master/samples/databases/northwind-pubs
        String sql = UtilsForTests.readFromResource("/sql/PUBS.sql");
        List<String> commands = Arrays.asList(sql.split("(?i)GO\\r\\n", -1));
        BaseTest.executeCommands(commands, con);

        BaseTest.executeCommand("USE PUBS", con);
    }

    public static void testSomething() {
        //Timestamp
        String v1 = "1994-02-17 10:23:43.9970000";
        String v2 = "1994-02-17 10:23:43.997";
        String v3 = "1994-02-17 10:23:43";
        String v4 = "1994-02-17";


        log.warn(Timestamp.valueOf(v1));
        log.warn(Timestamp.valueOf(v2));
        log.warn(Timestamp.valueOf(v3));
        boolean shouldFail = false;
        try {
            log.warn(Timestamp.valueOf(v4));
        } catch (IllegalArgumentException e) {
            shouldFail = true;
            log.warn(e);
        }
        assertTrue(shouldFail);
    }

    public void testStringFormatNull() {
        String message = null;
        log.error(String.format("%s", message));
    }
}
