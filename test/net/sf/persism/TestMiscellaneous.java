package net.sf.persism;

import junit.framework.TestCase;

import java.sql.*;

public class TestMiscellaneous extends TestCase {

    private static final Log log = Log.getLogger(TestMiscellaneous.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUnknownConnection() throws Exception {
        // this no longer fails. We just do log warn
    }
}
