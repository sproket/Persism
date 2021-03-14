package net.sf.persism;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class MSSQLDataSource {

    private static final Log log = Log.getLogger(MSSQLDataSource.class);


    private static MSSQLDataSource instance = null;

    private DataSource dataSource;

    private MSSQLDataSource(boolean mssqlmode) throws Exception {

        log.info("SQL MODE? " + mssqlmode);

        Properties props = new Properties();
        if (mssqlmode) {
            props.load(getClass().getResourceAsStream("/mssql.properties"));
        } else {
            props.load(getClass().getResourceAsStream("/jtds.properties"));
        }

        String driver = props.getProperty("database.driver");
        Class.forName(driver);
        initDatasource(props);
    }

    private void initDatasource(Properties props) throws Exception {

        String driver = props.getProperty("database.driver");
        String url = props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");

        GenericObjectPool connectionPool = new GenericObjectPool(null);

        Properties poolProps = new Properties();
        try (InputStream in = BaseTest.class.getResourceAsStream("/pool.properties")) {
            poolProps.load(in);
        }
        //if we don't see username, we fallback to windows authentication, so add only useNYLMv2
        if (username != null) {
            poolProps.setProperty("user", username);
            poolProps.setProperty("password", password);
        }

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, poolProps);

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1", false, true);
        dataSource = new PoolingDataSource(poolableConnectionFactory.getPool());
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static MSSQLDataSource getInstance() throws Exception {
        if (instance == null) {
            instance = new MSSQLDataSource(BaseTest.mssqlmode);
        }
        return instance;
    }

    public static void removeInstance() {
        instance = null;
    }
}
