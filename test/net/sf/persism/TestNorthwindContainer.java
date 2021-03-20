package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.LocalDB;
import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.northwind.*;
import org.junit.ClassRule;
import org.testcontainers.containers.MSSQLServerContainer;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;


/**
 * Does not share common tests - this is just to do some specific tests on SQL with Northwind DB
 *
 * @author Dan Howard
 * @since 5/3/12 8:46 PM
 */
@org.junit.experimental.categories.Category(TestContainerDB.class)
public class TestNorthwindContainer extends TestNorthwind {

    private static final Log log = Log.getLogger(TestNorthwindContainer.class);

    @ClassRule
    private static final MSSQLServerContainer<?> DB_CONTAINER = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2017-latest")
            .acceptLicense();

    Connection con;

    Session session;

    @Override
    protected void setUp() throws Exception {
        boolean mustCreateTables = false;
        if (!DB_CONTAINER.isRunning()) {
            //there are lots of warnings while this container starts, but it works.
            //it is an open issue: https://github.com/testcontainers/testcontainers-java/issues/3079
            DB_CONTAINER.start();
            mustCreateTables = true;
        }
//        super.setUp();

        Class.forName(DB_CONTAINER.getDriverClassName());

        con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());

        if (mustCreateTables) {
            createTables();
        } else {
            BaseTest.executeCommand("USE Northwind", con);
        }

        session = new Session(con);
    }

    private void createTables() throws SQLException {
        //from https://github.com/Microsoft/sql-server-samples/tree/master/samples/databases/northwind-pubs
        String sql = UtilsForTests.readFromResource("/sql/NORTHWIND.sql");
        List<String> commands = Arrays.asList(sql.split("(?i)GO\\r\\n", -1));
        BaseTest.executeCommands(commands, con);

        BaseTest.executeCommand("USE Northwind", con);
        BaseTest.executeCommand("ALTER TABLE Customers ADD DateOfLastResort datetime NULL", con);
        BaseTest.executeCommand("ALTER TABLE Customers ADD DateOfDoom datetime NULL", con);
        BaseTest.executeCommand("ALTER TABLE Customers ADD DateOfOffset datetime NULL", con);
        BaseTest.executeCommand("ALTER TABLE Customers ADD TestLocalDateTime datetime NULL", con);
        BaseTest.executeCommand("ALTER TABLE Customers ADD NowMF datetime NULL", con);
        BaseTest.executeCommand("ALTER TABLE Customers ADD wtfDate datetime NULL", con);
        BaseTest.executeCommand("ALTER TABLE Employees ADD status char (1) NULL", con);
        BaseTest.executeCommand("ALTER TABLE Employees ADD WhatTimeIsIt time NULL", con);
        BaseTest.executeCommand("ALTER TABLE Categories ADD data xml NULL", con);
    }

    @Override
    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }


}
