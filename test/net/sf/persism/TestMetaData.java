package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.LocalDB;
import net.sf.persism.dao.ByteData;
import net.sf.persism.dao.CustomerOrder;
import net.sf.persism.dao.OracleOrder;
import net.sf.persism.dao.records.CustomerOrderRec;
import org.junit.experimental.categories.Category;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.System.out;

@Category(LocalDB.class)
public final class TestMetaData extends TestCase {

    private static final Log log = Log.getLogger(TestMetaData.class);

    Connection con;
    Session session;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/derby.properties"));
        //Class.forName(props.getProperty("database.driver")).newInstance(); // derby needs new instance....

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
            assertEquals("Message s/b 'Could not determine a table for type: net.sf.persism.TestDerby Guesses were: [TestDerby, TestDerbies, Test Derby, Test_Derby, Test Derbies, Test_Derbies, Test Derbys, Test_Derbys] and we found multiple matching: [TEST_DERBY, TESTDERBY]'",
                    "Could not determine a table for type: net.sf.persism.TestDerby Guesses were: [TestDerby, TestDerbies, TestDerbys, Test Derby, Test_Derby, Test Derbies, Test_Derbies, Test Derbys, Test_Derbys] and we found multiple matching: [TEST_DERBY, TESTDERBY]",
                    e.getMessage());
        }
        assertTrue(failed);
    }

    public void testComments() {
        String sql = """
                -- works?
                /* how about this? */
                /*
                HOW 
                ABOUT 
                THIS?
                */
                   -- hello?
                SELECT * FROM RecordTest2 -- what about this?
                """;

        SQL sql1 = new SQL(sql);

        log.info("before: [" + sql + "]");
        log.info("after: " + sql1);

        // the comment after is fine.
        assertEquals("s/b 'SELECT * FROM RecordTest2 -- what about this?'", "SELECT * FROM RecordTest2 -- what about this?", sql1.toString());
        log.info("-------");
    }
    public void testDeterminePropertyInfo() {
        Collection<PropertyInfo> propertyInfo = MetaData.getPropertyInfo(ByteData.class);
        log.warn(propertyInfo.size());
    }

    public void testReflection() throws NoSuchFieldException, IllegalAccessException {
// https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/lang/invoke/MethodHandles.Lookup.html
//        MethodHandles.Lookup lookup = MethodHandles.lookup();
//        var xx = lookup.findGetter(CustomerOrder.class, "dateCreated", Date.class);
//        log.warn(xx);
//        var x = lookup.findGetter(CustomerOrderRec.class, "dateCreated", LocalDateTime.class);
//        log.warn(x);

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

        Method[] methods = classToTest.getMethods();
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
