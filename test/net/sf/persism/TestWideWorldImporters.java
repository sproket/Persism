package net.sf.persism;


import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Category(ExternalDB.class)
public class TestWideWorldImporters extends TestCase {

    private static final Log log = Log.getLogger(TestWideWorldImporters.class);

    Session session;
    Connection con;

    // this doesn't work - 2 issues
    // https://github.com/microsoft/mssql-jdbc/issues/1701
    // https://github.com/microsoft/mssql-jdbc/issues/656

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        con = DriverManager.getConnection("jdbc:sqlserver://localhost;database=WideWorldImporters;integratedSecurity=true;");
        session = new Session(con);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMetaData() throws Exception {
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
                System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
            }
            tables.add(rs.getString("TABLE_NAME"));
            System.out.println("----------");
        }

        for (String table : tables) {
            System.out.println("Table " + table + " COLUMN INFO");
            rs = dmd.getColumns(null, session.getMetaData().getConnectionType().getSchemaPattern(), table, null);
            rsmd = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.println(rsmd.getColumnName(i) + " = " + rs.getObject(i));
                }
                System.out.println("----------");
            }

        }

    }
}
