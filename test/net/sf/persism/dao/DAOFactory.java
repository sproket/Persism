package net.sf.persism.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class to wrap specific DAO objects to test specific issues with various dbs and jdbcs..
 *
 * @author Dan Howard
 * @since 6/16/12 8:39 AM
 */
public class DAOFactory {

    private DAOFactory() {
    }

    public static Order newOrder(Connection con)  {
        try {
            if (con.getMetaData().getDatabaseProductName().toUpperCase().contains("ORACLE")) {
                return new OracleOrder();
            } else {
                return new Order();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
