package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.access.Contact;
import net.ucanaccess.complex.Attachment;
import org.junit.experimental.categories.Category;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

// Marked as ExternalDB to prevent Maven from running it and failing on file in use
// on the Files.copy line. WTF. It runs fine with AllTests...
@Category(ExternalDB.class)
public class TestMSAccess extends TestCase {

    private static final Log log = Log.getLogger(TestMSAccess.class);
    public static final Attachment[] ATTACHMENTS = new Attachment[0];

    Connection con;

    Session session;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/msaccess.properties"));

        String home = UtilsForTests.createHomeFolder("msaccess");

        URI uri = getClass().getResource("/Contacts.accdb").toURI();

        Path from = Paths.get(uri);
        Path to = Paths.get(home + "/Contacts.accdb");
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

        String url = String.format(props.getProperty("database.url"), home + "/Contacts.accdb");
        log.info(url);

        con = DriverManager.getConnection(url);

        session = new Session(con);

        // listMetaData();
    }

    public void testContact() throws SQLException, IOException {
        // test Contacts.accddb should contain 1 row ID 1 with 2 attachments
        // Note Access fails with multiple test methods - so any other testing put here.
        List<Contact> list = session.query(Contact.class, "select * from Contacts");
        log.info(list);

        Contact contact;

        contact = new Contact();
        contact.setCompany("abc inc");
        contact.setFirstName("fred");
        contact.setLastName("flint");
        contact.setZIPPostalCode("12345");
        contact.setEmailAddress("x@y.com");
        contact.setCountryRegion("CA");
        contact.setStateProvince("XYZ");
        session.insert(contact);

        assertTrue("contact id > 0", contact.getId() > 0);
        assertEquals("id s/b 2", 2, contact.getId());

        log.info("created on  " + contact.getCreated());
        assertNotNull("created date defaulted?", contact.getCreated());

        list = session.query(Contact.class, "select * from Contacts");
        log.info(list);
        assertEquals("should be 2", 2, list.size());

        contact.setEmailAddress("x@Z.com");
        session.update(contact);

        contact = new Contact();
        contact.setId(1);
        session.fetch(contact);

        // net.ucanaccess.complex.Attachment type returned
        // You will see this type in the log.warn
        Attachment[] attachments = contact.getAttachments();
        log.info("attachments? " + attachments.length);
        for (int j = 0; j < attachments.length; j++) {
            Attachment attachment = attachments[j];
            log.info("url:  " + attachment.getUrl());
            log.info("type: " + attachment.getType());
            log.info("name: " + attachment.getName());
            log.info("time: " + attachment.getTimeStamp());
        }
        assertEquals("attachmens s/b/2", contact.getAttachments().length, 2);

        // add to the array?
        // attachments = contact.getAttachments();
        List<Attachment> attachmentList = new ArrayList<>(Arrays.asList(attachments));
        Attachment attachment = new Attachment(null, "test", "png", null, new Date(System.currentTimeMillis()), 0);

        BufferedImage img = ImageIO.read(getClass().getResourceAsStream("/logo1.png"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        attachment.setData(bytes);
        attachmentList.add(attachment);
        contact.setAttachments(attachmentList.toArray(ATTACHMENTS));

        assertEquals(contact.getJobTitle(), "Software Developer");
        contact.setJobTitle("Software Bug Creator!");
        session.update(contact);

        contact = new Contact();
        contact.setId(1);
        session.fetch(contact);

        assertEquals(contact.getJobTitle(), "Software Bug Creator!");

        assertEquals("attachmens s/b/3", contact.getAttachments().length, 3);

        contact.setId(1);
        assertTrue(session.fetch(contact));
        assertEquals("s/b 5.23", "5.2300", contact.getHowMuchTheyOweMe().toString());
    }

    @Override
    public void tearDown() throws Exception {
        session.close();
        con.close();
        super.tearDown();
    }

    private void listMetaData() throws SQLException {
        DatabaseMetaData dmd = con.getMetaData();
        log.info("GetDbMetaData for " + dmd.getDatabaseProductName());

        String[] tableTypes = {"TABLE"};

        ResultSetMetaData rsmd;
        ResultSet rs;
        // get attributes
        //rs = dmd.getAttributes("", "", "", "");
        List<String> tables = new ArrayList<>(32);
        rs = dmd.getTables(null, session.getMetaData().getConnectionType().getSchemaPattern(), null, tableTypes);
        rsmd = rs.getMetaData();
        while (rs.next()) {
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                log.info(rsmd.getColumnName(i) + " = " + rs.getObject(i));
            }
            tables.add(rs.getString("TABLE_NAME"));
            log.info("----------");
        }

        for (String table : tables) {
            log.info("Table " + table + " COLUMN INFO");
            rs = dmd.getColumns(null, session.getMetaData().getConnectionType().getSchemaPattern(), table, null);
            rsmd = rs.getMetaData();
            while (rs.next()) {
                log.info("COLUMN *** " + rs.getObject("COLUMN_NAME") + " ***");
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    log.info(rsmd.getColumnName(i) + " = " + rs.getObject(i));
                }
                log.info("----------");
            }

        }
    }


}
