package net.sf.persism;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Comments for MSSQLConnection go here.
 *
 * @author Dan Howard
 * @since 4/29/12 2:05 PM
 */
public class MSSQLConnection {

    private static final Log log = Log.getLogger(MSSQLConnection.class);

    public static void main(String[] args) {

        try {
            Properties props = new Properties();
            props.load(MSSQLConnection.class.getResourceAsStream("/mssql.properties"));
            String driver = props.getProperty("database.driver");
            String url = props.getProperty("database.url");
            String username = props.getProperty("database.username");
            String password = props.getProperty("database.password");
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, username, password);


            String[] tableTypes = {"TABLE"};

            ResultSet rs = con.getMetaData().getTables(null, "%", null, tableTypes);
            while (rs.next()) {
                log.info(rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
