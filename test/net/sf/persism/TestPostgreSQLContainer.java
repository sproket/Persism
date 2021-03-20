package net.sf.persism;

import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.Contact;
import net.sf.persism.dao.Customer;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Comments for TestPostgreSQL go here.
 *
 * @author Dan Howard
 * @since 6/21/12 6:05 AM
 */
@Category(TestContainerDB.class)
public final class TestPostgreSQLContainer extends TestPostgreSQL {

    private static final Log log = Log.getLogger(TestPostgreSQLContainer.class);

    @ClassRule
    private static final PostgreSQLContainer<?> DB_CONTAINER = new PostgreSQLContainer<>("postgres:9.6.12")
            .withUsername("pinf")
            .withPassword("pinf")
            .withDatabaseName("pinf");

    @Override
    protected void setUp() throws Exception {
        if(!DB_CONTAINER.isRunning()) {
            DB_CONTAINER.start();
        }
        connectionType = ConnectionTypes.PostgreSQL;
        super.setUp();

        log.info("PostgreSQLContainer");

        Class.forName(DB_CONTAINER.getDriverClassName());

        con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());

        createTables();

        session = new Session(con);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
