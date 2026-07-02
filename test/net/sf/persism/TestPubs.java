package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.pubs.Author;
import net.sf.persism.dao.pubs.JobType;
import net.sf.persism.dao.pubs.PublisherInfo;
import net.sf.persism.dao.pubs.PublisherTitle;
import net.sf.persism.logging.LogMode;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

import static net.sf.persism.SQL.sql;
import static net.sf.persism.SQL.where;

// Does not share common tests - this is just to do some specific tests on SQL with PUBS DB
@Category(ExternalDB.class)
public class TestPubs extends TestCase {

    private static final Log log = Log.getLogger(TestPubs.class, LogMode.LOG4J2);

    Connection con;
    static Session session;

    public void setUp() throws Exception {

        if (getClass().equals(TestPubs.class)) {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/pubs.properties"));
            String driver = props.getProperty("database.driver");
            String url = props.getProperty("database.url");
            String username = props.getProperty("database.username");
            String password = props.getProperty("database.password");
            Class.forName(driver);

            con = DriverManager.getConnection(url, username, password);

            session = new Session(con);
        }
        log.debug("1");
        log.info("1");
        log.warn("1");
        log.warn("1", new Throwable("n/a"));
        log.error("1");
        log.error("1", new Throwable("n/a"));
    }

    public void tearDown() throws Exception {
        con.close();
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
            } else {
                author.setFirstName("Dan");
                author.setLastName("Howard");
                author.setAddress("123 Sesame Street");
                author.setCity("MTL");
                author.setState("OH");
                author.setPostalCode("45143");
                author.setContract(true);

                session.update(author);
                session.fetch(author);
            }


            // test constraints
            // phone defaults to UNKNOWN
//            assertEquals("Phone should be UNKNOWN (char(12))", "UNKNOWN     ", author.getPhone());
            Assert.assertTrue("contract s/b true", author.isContract());
            Assert.assertEquals("First name Dan", "Dan", author.getFirstName());
            Assert.assertEquals("Last name Howard", "Howard", author.getLastName());
            Assert.assertEquals("Street 123 Sesame Street...", "123 Sesame Street", author.getAddress());
            Assert.assertEquals("City MTL...", "MTL", author.getCity());
            Assert.assertEquals("State OH...", "OH", author.getState());
            Assert.assertEquals("zip 45143...", "45143", author.getPostalCode());


            log.info(author);

            // postal code needs to be 5 digits
            author.setPostalCode("BLAH!");
            boolean constraintFailed = false;
            try {
                session.update(author); // should fail
            } catch (PersismException e) {
                constraintFailed = true;
                log.error(e.getMessage());
                Assert.assertTrue("should contain 'The UPDATE statement conflicted with the CHECK constraint'", e.getMessage().contains("The UPDATE statement conflicted with the CHECK constraint"));
            }
            Assert.assertTrue("phone constraint should fail", constraintFailed);

            List<Author> list = session.query(Author.class, sql("Select * From authors"));
            log.info(list.size());

            List<PublisherInfo> publishers = session.query(PublisherInfo.class, sql("select * from pub_info"));
            log.info(publishers);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        } finally {
            session.delete(author);
        }
    }

    public void testTopQuery() {
        long now = System.currentTimeMillis();
        List<PublisherTitle> publisherTitles = session.query(PublisherTitle.class, where("1=1").limit(4));

        log.info(publisherTitles.size());
        log.info("time: " + (System.currentTimeMillis() - now));

    }


    public void testJobTypes() {
        List<JobType> jobs = session.query(JobType.class, sql("select * from jobs"));
        log.info(jobs);
        log.info(jobs.size());

        JobType jobType = new JobType();
        jobType.setJobId(4);
        // JobType{jobId=4, description='Chief Financial Officier', minLevel=175, maxLevel=250},
        Assert.assertTrue("should be found", session.fetch(jobType));

        // lets fix the spelling error
        jobType.setDescription("Chief Financial Officer");

        session.update(jobType);
        Assert.assertTrue("should be found", session.fetch(jobType)); // dont need to do this. just testing reading again

        Assert.assertEquals("description s/b ", "Chief Financial Officer", jobType.getDescription());
        Assert.assertEquals("min lvl s/b ", 175, jobType.getMinLevel());
        Assert.assertEquals("max lvl s/b ", 250, jobType.getMaxLevel());

    }
}
