/**
 * Comments for TestPubs go here.
 *
 * @author Dan Howard
 * @since 5/25/12 5:28 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.pubs.Author;
import net.sf.persism.dao.pubs.JobType;
import net.sf.persism.dao.pubs.PublisherInfo;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.MSSQLServerContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

// Does not share common tests - this is just to do some specific tests on SQL with PUBS DB
@Category(TestContainerDB.class)
public class TestPubsContainer extends TestPubs {

    private static final Log log = Log.getLogger(TestPubsContainer.class);

    @ClassRule
    private static final MSSQLServerContainer<?> DB_CONTAINER = new MSSQLServerContainer <>("mcr.microsoft.com/mssql/server:2017-latest")
            .acceptLicense();

    Connection con;
    Session session;

    @Override
    protected void setUp() throws Exception {
        boolean mustCreateTables = false;
        if(!DB_CONTAINER.isRunning()) {
            //there are lots of warnings while this container starts, but it works.
            //it is an open issue: https://github.com/testcontainers/testcontainers-java/issues/3079
            DB_CONTAINER.start();
            mustCreateTables = true;
        }
        //super.setUp();

        Class.forName(DB_CONTAINER.getDriverClassName());

        con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());

        if(mustCreateTables) {
            createTables();
        } else{
            BaseTest.executeCommand("USE PUBS", con);
        }

        session = new Session(con);
    }

    private void createTables() throws SQLException {
        //from https://github.com/Microsoft/sql-server-samples/tree/master/samples/databases/northwind-pubs
        String sql = UtilsForTests.readFromResource("/sql/PUBS.sql");
        List<String> commands = Arrays.asList(sql.split("(?i)GO\\r\\n", -1));
        BaseTest.executeCommands(commands, con);

        BaseTest.executeCommand("USE PUBS", con);
    }

    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }
}
