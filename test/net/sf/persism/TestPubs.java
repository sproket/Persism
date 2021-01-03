/**
 * Comments for TestPubs go here.
 *
 * @author Dan Howard
 * @since 5/25/12 5:28 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
//import net.sf.log4jdbc.ConnectionSpy;
import net.sf.log4jdbc.log.SpyLogDelegator;
import net.sf.log4jdbc.sql.Spy;
import net.sf.log4jdbc.sql.jdbcapi.ConnectionSpy;
import net.sf.log4jdbc.sql.resultsetcollector.ResultSetCollector;
import net.sf.persism.dao.pubs.Author;
import net.sf.persism.dao.pubs.JobType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

// Does not share common tests - this is just to do some specific tests on SQL with PUBS DB
public class TestPubs extends TestCase {

    private static final Log log = Log.getLogger(TestPubs.class);

    Connection con;
    Query query;
    Command command;


    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/pubs.properties"));
        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");
        Class.forName(driver);

        con = DriverManager.getConnection(url, username, password);

        con = new ConnectionSpy(con, new SpyLogDelegator() {
            @Override
            public boolean isJdbcLoggingEnabled() {
                return true;
            }

            @Override
            public void exceptionOccured(Spy spy, String s, Exception e, String s1, long l) {
                log.error(s, e);
            }

            @Override
            public void methodReturned(Spy spy, String s, String s1) {
                log.info(s);
            }

            @Override
            public void constructorReturned(Spy spy, String s) {
                log.info(s);
            }

            @Override
            public void sqlOccurred(Spy spy, String s, String s1) {
                log.info(s);
            }

            @Override
            public void sqlTimingOccurred(Spy spy, long l, String s, String s1) {
                log.info(s);
            }

            @Override
            public void connectionOpened(Spy spy, long l) {
                log.info(l);
            }

            @Override
            public void connectionClosed(Spy spy, long l) {
                log.info(l);
            }

            @Override
            public void connectionAborted(Spy spy, long l) {
                log.info(l);
            }

            @Override
            public void debug(String s) {
                log.info(s);
            }

            @Override
            public boolean isResultSetCollectionEnabled() {
                return true;
            }

            @Override
            public boolean isResultSetCollectionEnabledWithUnreadValueFillIn() {
                return true;
            }

            @Override
            public void resultSetCollected(ResultSetCollector resultSetCollector) {
                log.info("resultSetCollected");
            }
        });

        query = new Query(con);
        command = new Command(con);

    }

    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }

    public void testAuthors() {

        Author author = new Author();
        author.setAuthorId("888-11-8888"); // ID Needs to look like 999-99-9999 or constraint exception

        try {
            if (!query.read(author)) {
                author.setFirstName("Dan");
                author.setLastName("Howard");
                author.setAddress("123 Sesame Street");
                author.setCity("MTL");
                author.setState("OH");
                author.setPostalCode("45143");
                author.setContract(true);

                command.insert(author);
            }


            // test constraints
            // phone defaults to UNKNOWN
            assertEquals("Phone should be UNKNOWN (char(12))", "UNKNOWN     ", author.getPhone());
            assertTrue("contract s/b true", author.isContract());
            assertEquals("First name Dan", "Dan", author.getFirstName());
            assertEquals("Last name Howard", "Howard", author.getLastName());
            assertEquals("Street 123 Sesame Street...", "123 Sesame Street", author.getAddress());
            assertEquals("City MTL...", "MTL", author.getCity());
            assertEquals("State OH...", "OH", author.getState());
            assertEquals("zip 45143...", "45143", author.getPostalCode());


            log.info(author);

            // postal code needs to be 5 digits
            author.setPostalCode("BLAH BLAH BLAH");
            boolean constraintFailed = false;
            try {
                command.update(author); // should fail
            } catch (PersismException e) {
                constraintFailed = true;
                assertTrue("should contain 'The UPDATE statement conflicted with the CHECK constraint'", e.getMessage().contains("The UPDATE statement conflicted with the CHECK constraint"));
            }
            assertTrue("phone constraint should fail", constraintFailed);

            List<Author> list = query.readList(Author.class, "Select * From authors");
            log.info(list.size());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            command.delete(author);
        }
    }

    public void testJobTypes() {
        List<JobType> jobs = query.readList(JobType.class, "select * from jobs");
        log.info(jobs);
        log.info(jobs.size());

        JobType jobType = new JobType();
        jobType.setJobId(4);
        // JobType{jobId=4, description='Chief Financial Officier', minLevel=175, maxLevel=250},
        assertTrue("should be found", query.read(jobType));

        // lets fix the spelling error
        jobType.setDescription("Chief Financial Officer");
        command.update(jobType);
        assertTrue("should be found", query.read(jobType)); // dont need to do this. just testing reading again

        assertEquals("description s/b ", "Chief Financial Officer", jobType.getDescription());
        assertEquals("min lvl s/b ", 175, jobType.getMinLevel());
        assertEquals("max lvl s/b ", 250, jobType.getMaxLevel());

    }
}
