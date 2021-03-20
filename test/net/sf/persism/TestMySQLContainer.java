package net.sf.persism;

import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.Customer;
import net.sf.persism.dao.Regions;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.MySQLContainer;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dan Howard
 * @since 6/4/12 9:52 PM
 */
@Category(TestContainerDB.class)
public final class TestMySQLContainer extends TestMySQL {

    private static final Log log = Log.getLogger(TestMySQLContainer.class);

    @ClassRule
    private static final MySQLContainer<?> DB_CONTAINER = new MySQLContainer<>("mysql:5.7.22")
            .withUsername("pinf")
            .withPassword("pinf")
            .withDatabaseName("pinf");

    @Override
    protected void setUp() throws Exception {
        if(!DB_CONTAINER.isRunning()) {
            DB_CONTAINER.start();
        }
        connectionType = ConnectionTypes.MySQL;
        super.setUp();

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
