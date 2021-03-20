package net.sf.persism;

import net.sf.persism.categories.TestContainerDB;
import org.firebirdsql.testcontainers.FirebirdContainer;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;

@Category(TestContainerDB.class)
public final class TestFirebirdContainer extends TestFirebird {

    private static final Log log = Log.getLogger(TestFirebirdContainer.class);

    private static final DockerImageName IMAGE =
            DockerImageName.parse(FirebirdContainer.IMAGE).withTag("3.0.7");

    @ClassRule
    public static final FirebirdContainer<?> DB_CONTAINER = new FirebirdContainer<>(IMAGE)
            .withUsername("pinf")
            .withPassword("pinf");

    @Override
    public void setUp() throws Exception {

        connectionType = ConnectionTypes.Firebird;
        super.setUp();

        Class.forName(DB_CONTAINER.getDriverClassName());

        con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());
        log.info(con.getMetaData().getDatabaseProductName());

        createTables();

        session = new Session(con);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


}
