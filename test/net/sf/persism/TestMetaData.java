/**
 * Comments for TestMetaData go here.
 *
 * @author Dan Howard
 * @since 7/31/13 6:31 AM
 */
package net.sf.persism;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public final class TestMetaData extends BaseTest {
    private static final Log log = Log.getLogger(TestMetaData.class);

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/derby.properties"));
        Class.forName(props.getProperty("database.driver")).newInstance(); // derby needs new instance....

        String home = UtilsForTests.createHomeFolder("pinfderby");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url);

        createTables();

        session = new Session(con);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void createTables() throws SQLException {
        List<String> commands = new ArrayList<String>(12);
        String sql;
        if (UtilsForTests.isTableInDatabase("TestDerby", con)) {
            sql = "DROP TABLE TestDerby";
            commands.add(sql);
        }
        if (UtilsForTests.isTableInDatabase("Test_Derby", con)) {
            sql = "DROP TABLE Test_Derby";
            commands.add(sql);
        }
        if (UtilsForTests.isTableInDatabase("DB_TEST_DERBY", con)) {
            sql = "DROP TABLE DB_TEST_DERBY";
            commands.add(sql);
        }

        sql = "CREATE TABLE TestDerby ( " +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) " +
                ") ";

        commands.add(sql);

        sql = "CREATE TABLE Test_Derby ( " +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) " +
                ") ";

        commands.add(sql);

        sql = "CREATE TABLE DB_Test_Derby ( " +
                "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) " +
                ") ";

        commands.add(sql);

        executeCommands(commands, con);
    }

    public void testGuessing() throws SQLException {

        // catch the 2 guess exceptions
        /*
            throw new PersismException("Could not determine a table for type: " + objectClass.getName() + " Guesses were: " + guesses);
            throw new PersismException("Could not determine a table for type: " + objectClass.getName() + " Guesses were: " + guesses + " and we found multiple matching tables: " + guessedTables);
         */
        // Note I just picked 2 rando classes to try to use TestMetaData and TestDerby.
        // It could be any old class that doesn't make sense to insert into a database.
        boolean failed = false;
        try {
            session.insert(new TestMetaData());
        } catch (PersismException e) {
            failed = true;
            assertEquals("Message s/b 'Could not determine a table for type: net.sf.persism.TestMetaData Guesses were: [TestMetaData, TestMetaDatas, Test Meta Data, Test_Meta_Data, Test Meta Datas, Test_Meta_Datas]'",
                    "Could not determine a table for type: net.sf.persism.TestMetaData Guesses were: [TestMetaData, TestMetaDatas, Test Meta Data, Test_Meta_Data, Test Meta Datas, Test_Meta_Datas]",
                    e.getMessage());
        }
        assertTrue(failed);

        failed = false;
        try {
            session.insert(new TestDerby());
        } catch (PersismException e) {
            failed = true;
            assertEquals("Message s/b 'Could not determine a table for type: net.sf.persism.TestDerby Guesses were: [TestDerby, TestDerbies, Test Derby, Test_Derby, Test Derbies, Test_Derbies] and we found multiple matching tables: [TEST_DERBY, TESTDERBY]'",
                    "Could not determine a table for type: net.sf.persism.TestDerby Guesses were: [TestDerby, TestDerbies, Test Derby, Test_Derby, Test Derbies, Test_Derbies] and we found multiple matching tables: [TEST_DERBY, TESTDERBY]",
                    e.getMessage());
        }
        assertTrue(failed);
    }


    public void testNamedParams() {
        try {
            HashMap indexMap = new HashMap();

            String query = "select * from people where (first_name = :name or last_name = :name) and address = :address";
            String parsedQuery = parse(query, indexMap);
            log.info(parsedQuery);
            log.info(indexMap);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }


    }


    /**
     * Adam Crume, JavaWorld.com, 04/03/07
     * http://www.javaworld.com/javaworld/jw-04-2007/jw-04-jdbc.html?page=2
     * Parses a query with named parameters.  The parameter-index mappings are
     * put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This
     * method is non-private so JUnit code can
     * test it.
     *
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    static String parse(String query, Map paramMap) {
        // I was originally using regular expressions, but they didn't work well
        // for ignoring parameter-like strings inside quotes.
        int length = query.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length &&
                        Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter

                    List indexList = (List) paramMap.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));

                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // replace the lists of Integer objects with arrays of ints
        for (Iterator itr = paramMap.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry entry = (Map.Entry) itr.next();
            List list = (List) entry.getValue();
            int[] indexes = new int[list.size()];
            int i = 0;
            for (Iterator itr2 = list.iterator(); itr2.hasNext(); ) {
                Integer x = (Integer) itr2.next();
                indexes[i++] = x.intValue();
            }
            entry.setValue(indexes);
        }

        return parsedQuery.toString();
    }

    @Override
    public void testDates() {
        // comes from BaseTest - we don't need it here
    }

    @Override
    public void testStoredProcs() {
        // comes from BaseTest - we don't need it here
    }

    @Override
    public void testRefreshObject() {
        // comes from BaseTest - we don't need it here
    }

    @Override
    public void testQueryWithSpecificColumnsWhereCaseDoesNotMatch() throws SQLException {
        // comes from BaseTest - we don't need it here
    }

    @Override
    public void testQueryResult() {
        // comes from BaseTest - we don't need it here
    }

    @Override
    public void testReadPrimitive() {
        // comes from BaseTest - we don't need it here
    }
}
