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
import java.util.*;

// Does not share common tests - this is just to do some specific tests on SQL with PUBS DB
@Category(TestContainerDB.class)
public class TestPubs extends TestCase {

    private static final Log log = Log.getLogger(TestPubs.class);

    @ClassRule
    private static final MSSQLServerContainer<?> DB_CONTAINER = new MSSQLServerContainer <>("mcr.microsoft.com/mssql/server:2017-latest")
            .acceptLicense();

    Connection con;
    Session session;

    protected void setUp() throws Exception {
        boolean mustCreateTables = false;
        if(!DB_CONTAINER.isRunning()) {
            //there are lots of warnings while this container starts, but it works.
            //it is an open issue: https://github.com/testcontainers/testcontainers-java/issues/3079
            DB_CONTAINER.start();
            mustCreateTables = true;
        }
        super.setUp();

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

    public void testAuthors() {

        Author author = new Author();
        author.setAuthorId("888-11-8888"); // ID Needs to look like 999-99-9999 or constraint exception

        try {
            if (!session.fetch(author)) {
                author.setFirstName("Dan");
                author.setLastName("Howard");
                author.setAddress("123 Sesame Street");
                author.setCity("MTL");
                author.setState("OH");
                author.setPostalCode("45143");
                author.setContract(true);

                session.insert(author);
            }


            // test constraints
            // phone defaults to UNKNOWN
//            assertEquals("Phone should be UNKNOWN (char(12))", "UNKNOWN     ", author.getPhone());
            assertTrue("contract s/b true", author.isContract());
            assertEquals("First name Dan", "Dan", author.getFirstName());
            assertEquals("Last name Howard", "Howard", author.getLastName());
            assertEquals("Street 123 Sesame Street...", "123 Sesame Street", author.getAddress());
            assertEquals("City MTL...", "MTL", author.getCity());
            assertEquals("State OH...", "OH", author.getState());
            assertEquals("zip 45143...", "45143", author.getPostalCode());


            log.info(author);

            // postal code needs to be 5 digits
            author.setPostalCode("BLAH!");
            boolean constraintFailed = false;
            try {
                session.update(author); // should fail
            } catch (PersismException e) {
                constraintFailed = true;
                log.error(e.getMessage());
                assertTrue("should contain 'The UPDATE statement conflicted with the CHECK constraint'", e.getMessage().contains("The UPDATE statement conflicted with the CHECK constraint"));
            }
            assertTrue("phone constraint should fail", constraintFailed);

            List<Author> list = session.query(Author.class, "Select * From authors");
            log.info(list.size());

            List<PublisherInfo> publishers = session.query(PublisherInfo.class, "select * from pub_info");
            log.info(publishers);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            session.delete(author);
        }
    }

    public void testJobTypes() {
        List<JobType> jobs = session.query(JobType.class, "select * from jobs");
        log.info(jobs);
        log.info(jobs.size());

        JobType jobType = new JobType();
        jobType.setJobId(4);
        // JobType{jobId=4, description='Chief Financial Officier', minLevel=175, maxLevel=250},
        assertTrue("should be found", session.fetch(jobType));

        // lets fix the spelling error
        jobType.setDescription("Chief Financial Officer");

        session.update(jobType);
        assertTrue("should be found", session.fetch(jobType)); // dont need to do this. just testing reading again

        assertEquals("description s/b ", "Chief Financial Officer", jobType.getDescription());
        assertEquals("min lvl s/b ", 175, jobType.getMinLevel());
        assertEquals("max lvl s/b ", 250, jobType.getMaxLevel());

    }
}
