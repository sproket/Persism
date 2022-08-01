package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.Region;
import net.sf.persism.dao.access.Contact;
import net.sf.persism.dao.access.Customer;
import net.ucanaccess.complex.Attachment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static net.sf.persism.BaseTest.executeCommand;
import static net.sf.persism.Parameters.*;
import static net.sf.persism.SQL.*;
import static net.sf.persism.UtilsForTests.isTableInDatabase;

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

        URI uri = Objects.requireNonNull(getClass().getResource("/Contacts.accdb")).toURI();

        Path from = Paths.get(uri);
        Path to = Paths.get(home + "/Contacts.accdb");
//        if (!to.toFile().exists()) {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
//        }

        String url = String.format(props.getProperty("database.url"), home + "/Contacts.accdb");
        log.info(url);

        con = DriverManager.getConnection(url);
        createTables();

        session = new Session(con);
    }

    private void createTables() throws SQLException {
        String sql;

        if (isTableInDatabase("Customers", con)) {
            sql = "DROP TABLE Customers";
            executeCommand(sql, con);
        }

        // todo " and ' and [ and ] and `
        sql = """
                CREATE TABLE Customers (\s
                 `Customer ID` varchar(10) PRIMARY KEY NOT NULL,\s
                 `Company Name` VARCHAR(30) NULL,\s
                 `Contact : / @ # Name` VARCHAR(30) NULL,\s 
                 `Contact Title` VARCHAR(10) NULL,\s
                 `Address` VARCHAR(40) NULL,\s
                 `City` VARCHAR(30) NULL,\s
                 `Region` VARCHAR(10) NULL,\s
                 `Postal Code` VARCHAR(10) NULL,\s
                 `Country` VARCHAR(2) DEFAULT 'US',\s
                 `Phone` VARCHAR(30) NULL,\s
                 `Fax` VARCHAR(30) NULL,\s
                 `Status` CHAR(1) NULL,\s
                 `Date Registered` Timestamp DEFAULT NOW(),\s
                 `Date Of Last Order` DATE,\s
                 `Test Local Date` date,\s
                 `TestLocalDateTime` Timestamp\s
                )
                """;
        // Somehow Access Makes the column `Test Local DateTime] as `TEST LOCAL TIMESTAMP] WTF! - REMOVED SPACES TO FIX IT
        // filed https://sourceforge.net/p/jackcess/bugs/155/

        executeCommand(sql, con);
    }

    public void testCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setAddress("123 Sesame Street");
        customer.setCity("MTL");
        customer.setCompanyName("ABC Inc");
        customer.setContactName("Fred");
        customer.setContactTitle("LORD");
        customer.setCountry("US");

        customer.setFax("fax");
        customer.setPhone("phone");
        customer.setPostalCode("12345");
        customer.setRegion(Region.East);
        customer.setStatus('2');

        session.insert(customer);


        List<Customer> customers = session.query(Customer.class);

        log.info(customers.size());
        assertTrue(customers.size() > 0);

        customers = session.query(Customer.class, where(":city = @city AND :contactName = @contact"), params(Map.of("city", "MTL", "contact", "Fred")));
        log.info(customers.size());
        assertTrue(customers.size() > 0);

        // Same with full query
        String query = """
                SELECT *
                FROM Customers
                WHERE City = @city AND `Contact : / @ # Name` = @contact
                """;

        assertTrue(session.helper.isSelect(query));

        customers = session.query(Customer.class, sql(query), params(Map.of("city", "MTL", "contact", "Fred")));
        log.info(customers.size());
        assertTrue(customers.size() > 0);

    }

    public void testContact() throws SQLException, IOException {
        // test Contacts.accddb should contain 1 row ID 1 with 2 attachments
        // Note Access fails with multiple test methods - so any other testing put here.
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
        assertEquals("id s/b 2", 2, contact.getId()); // todo we're not resetting the DB?

        log.info("created on  " + contact.getCreated());
        assertNotNull("created date defaulted?", contact.getCreated());

        List<Contact> list = session.query(Contact.class);
        log.info(list);
        assertEquals("should be 2", 2, list.size()); // todo we're not resetting the DB?

        contact.setEmailAddress("x@Z.com");
        session.update(contact);

        contact = new Contact();
        contact.setId(1);
        session.fetch(contact);

        // net.ucanaccess.complex.Attachment type returned
        // You will see this type in the log.warn
        Attachment[] attachments = (Attachment[]) contact.getAttachments();
        log.info("attachments? " + attachments.length);
        for (int j = 0; j < attachments.length; j++) {
            Attachment attachment = attachments[j];
            log.info("url:  " + attachment.getUrl());
            log.info("type: " + attachment.getType());
            log.info("name: " + attachment.getName());
            log.info("time: " + attachment.getTimeStamp());
        }
        assertEquals("attachmens s/b/2", attachments.length, 2); // todo we're not resetting the DB?

        // add to the array?
        // attachments = contact.getAttachments();
        List<Attachment> attachmentList = new ArrayList<>(Arrays.asList(attachments));
        Attachment attachment = new Attachment(null, "test", "png", null, LocalDateTime.now(), 0);

        BufferedImage img = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/logo1.png")));
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

        assertEquals("attachmens s/b/3", ((Attachment[])contact.getAttachments()).length, 3);

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
