package net.sf.persism;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 6:10 AM
 */

import net.sf.persism.categories.TestContainerDB;
import net.sf.persism.dao.*;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.MSSQLServerContainer;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static net.sf.persism.UtilsForTests.*;

@Category(TestContainerDB.class)
public final class TestMSSQLContainer extends TestMSSQL {

    private static final Log log = Log.getLogger(TestMSSQLContainer.class);

    @ClassRule
    private static final MSSQLServerContainer<?> DB_CONTAINER = new MSSQLServerContainer <>("mcr.microsoft.com/mssql/server:2017-latest")
            .acceptLicense();

    @Override
    protected void setUp() throws Exception {
        boolean mustCreateTables = false;
        if(!DB_CONTAINER.isRunning()) {
            //there are lots of warnings while this container starts, but it works.
            //it is an open issue: https://github.com/testcontainers/testcontainers-java/issues/3079
            DB_CONTAINER.start();
            mustCreateTables = true;
        }

        connectionType = ConnectionTypes.MSSQL;
        Class.forName(DB_CONTAINER.getDriverClassName());


        super.setUp();
        log.info("SQLMODE? " + BaseTest.mssqlmode);
        log.info("dbContainer.getJdbcUrl(): " + DB_CONTAINER.getJdbcUrl());
        con = DriverManager.getConnection(DB_CONTAINER.getJdbcUrl(), DB_CONTAINER.getUsername(), DB_CONTAINER.getPassword());
        log.info("PRODUCT? " + con.getMetaData().getDriverName() + " - " + con.getMetaData().getDriverVersion());

        if(mustCreateTables) {
            createTables();
        }
        session = new Session(con);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}