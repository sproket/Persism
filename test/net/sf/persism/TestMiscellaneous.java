package net.sf.persism;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.Extension;
import com.mysql.jdbc.MySQLConnection;
import junit.framework.TestCase;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class TestMiscellaneous extends TestCase {

    private static final Log log = Log.getLogger(TestMiscellaneous.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static void testSomething() {
        //Timestamp
        String v1 = "1994-02-17 10:23:43.9970000";
        String v2 = "1994-02-17 10:23:43.997";
        String v3 = "1994-02-17 10:23:43";
        String v4 = "1994-02-17";


        log.warn(Timestamp.valueOf(v1));
        log.warn(Timestamp.valueOf(v2));
        log.warn(Timestamp.valueOf(v3));
        try {
            log.warn(Timestamp.valueOf(v4));
        } catch (IllegalArgumentException e) {
            log.info(e);
        }
    }
}
