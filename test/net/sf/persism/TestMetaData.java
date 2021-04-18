package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.ByteData;
import net.sf.persism.dao.Contact;
import net.sf.persism.dao.OracleOrder;
import net.sf.persism.dao.Order;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.out;

public final class TestMetaData extends TestCase {

    private static final Log log = Log.getLogger(TestMetaData.class);

    Connection con;
    Session session;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/derby.properties"));
        Class.forName(props.getProperty("database.driver")).newInstance(); // derby needs new instance....

        String home = UtilsForTests.createHomeFolder("pinfderby");
        String url = UtilsForTests.replace(props.getProperty("database.url"), "{$home}", home);
        log.info(url);

        con = DriverManager.getConnection(url);

        session = new Session(con);
    }


    public void testGuessing() {

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

    public void testDeterminePropertyInfo() {
        Collection<PropertyInfo> propertyInfo = MetaData.getPropertyInfo(ByteData.class);
        log.warn(propertyInfo.size());
    }

    public void testReflection() {

        Class<?> classToTest = OracleOrder.class;

        Map<String, PropertyInfo> propertyNames = new HashMap<>(32);

        List<Field> fields = new ArrayList<>(32);

        // getDeclaredFields does not get fields from super classes.....
        fields.addAll(Arrays.asList(classToTest.getDeclaredFields()));
        Class<?> sup = classToTest.getSuperclass();
        log.warn(sup);
        while (!sup.equals(Object.class)) {
            fields.addAll(Arrays.asList(sup.getDeclaredFields()));
            sup = sup.getSuperclass();
            log.warn(sup);
        }

        List<Method> methods = Arrays.asList(classToTest.getMethods());

        fields.forEach((field) -> {
            out.println("Field Name: " + field.getName());
            // Candidate getters
            String propertyName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            out.println("Property Name: *" + propertyName + "* ");

            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.propertyName = propertyName;
            propertyInfo.field = field;
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                propertyInfo.annotations.put(annotation.annotationType(), annotation);
            }

            for (Method method : methods) {
                String propertyNameToTest = propertyName;
                if (propertyNameToTest.startsWith("Is") && propertyNameToTest.length() > 2 && Character.isUpperCase(propertyNameToTest.charAt(2))) {
                    propertyNameToTest = propertyName.substring(2);
                }
                //out.println("PROPERTY NAME TO TEST: " + propertyNameToTest);

                if (method.getName().toLowerCase().endsWith(propertyNameToTest.toLowerCase()) && method.getName().length() - propertyNameToTest.length() <= 3) {
                    //out.println(method.getName().length() - propertyNameToTest.length() > 3);
                    out.println("METHOD: " + method.getName() + " " + (method.getName().length() - propertyNameToTest.length())); // found it! MYABE?

                    annotations = method.getAnnotations();
                    for (Annotation annotation : annotations) {
                        propertyInfo.annotations.put(annotation.annotationType(), annotation);
                    }

                    if (method.getName().equalsIgnoreCase("set" + propertyNameToTest)) {
                        propertyInfo.setter = method;
                    } else {
                        propertyInfo.getter = method;
                    }
                }
            }

            propertyInfo.readOnly = propertyInfo.setter == null;
            propertyNames.put(propertyName, propertyInfo);
        });

        out.println("--------------------");
//
//        for (Field field : fields) {
//            out.println("**" + field.getName() + "**");
//            methods.stream().filter(m -> m.getName().equals(field.getName()) || m.getName().toLowerCase().contains(field.getName().toLowerCase())).forEach(out::println);
//        }

    }
}
