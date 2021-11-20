/**
 * Comments for TestUtil go here.
 *
 * @author Dan Howard
 * @since 5/24/12 5:33 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.Customer;
import net.sf.persism.dao.Invoice;
import net.sf.persism.dao.Postman;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class TestUtil extends TestCase {

    private static final Log log = Log.getLogger(TestUtil.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReplaceAll() {
        String text = "this is a test";
        text = text.replaceAll(" ", "_");
        log.info(text);
        assertEquals("s/b 'this is a test'", "this_is_a_test", text);
    }

    public void testSet() {
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.add("ONE");
        set.add("two");
        set.add("THREE");
        set.add("TwO");
        // And your equals conditions should work without any issue
        assertEquals("should be 3?", 3, set.size());

        assertTrue(set.contains("TWO"));
    }


    public void testCamel() {
        try {
            log.info(Util.camelToTitleCase("OrderDetails"));
            log.info(Util.camelToTitleCase("orderDetailsFromJAVA"));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testJunk() {
        Postman postman = new Postman().
                host("blah").
                port(80).
                user("x").
                password("123");

        log.info(postman);


        List<String> stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("b");
        stringList.add("c");

        stringList.stream().forEach(str -> {
            if (str.equals("b")) {
                return; // only skips this iteration.
            }

            System.out.println(str);
        });
    }

    public void testFieldReflection() {
        // https://docs.oracle.com/javase/tutorial/reflect/member/fieldModifiers.html

        FieldModifierSpy.spy(Invoice.class, "final", "private");

    }

    public void testMod() {
        int count = 6;
        System.out.println(count / 1);
        System.out.println(count / 2);
        System.out.println(count / 3);

        float x = 1.0f;
        double y = 2.0d;
        int z = 3;

        System.out.println((int) x);
        System.out.println((int) y);
        System.out.println((int) z);
    }

    @Test
    public void testMessages() {
        String divName, heading1, heading2;
        divName = "1";
        heading1 = "h";
        heading2 = new Date().toString();

        String result = String.format("%-30s %-30s %-30s", divName, heading1, heading2);
        log.info(result);

        result = String.format("a b c %o %o", 1, 2);
        log.info(result);

        log.info(Messages.ObjectNotProperlyInitialized.message("Junk", "col, col2m, cop;le"));

        log.warn(Messages.UnknownSQLType.message(1));
        log.warn(Messages.ConverterValueTypeNotYetSupported.message(Types.InstantType.getJavaType()));
    }

    public void testStringToArray() {
        String s1 = "1,2,3";
        String s2 = "dan";

        String[] a1 = s1.split(",");
        log.info(Arrays.asList(a1));

        String[] a2 = s2.split(",");
        log.info(Arrays.asList(a2));


        String s = null;
        System.out.println(s);
        System.out.println("" + s);

        String s3 = null;
        String s4 = null;

        log.warn(Objects.equals(s3, s4));
    }

    List<String> stringList = new ArrayList<String>();
    List<Integer> integerList = new ArrayList<Integer>();
    List<Customer> custList = new ArrayList<Customer>();

    public void testGeneric() throws NoSuchFieldException {

        // this is neat but won't work for me.

        Field stringListField = getClass().getDeclaredField("stringList");
        ParameterizedType stringListType = (ParameterizedType) stringListField.getGenericType();
        Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        System.out.println(stringListClass); // class java.lang.String.

        Field integerListField = getClass().getDeclaredField("integerList");
        ParameterizedType integerListType = (ParameterizedType) integerListField.getGenericType();
        Class<?> integerListClass = (Class<?>) integerListType.getActualTypeArguments()[0];
        System.out.println(integerListClass); // class java.lang.Integer.

        Field custListField = getClass().getDeclaredField("custList");
        ParameterizedType custListType = (ParameterizedType) custListField.getGenericType();
        Class<?> custListClass = (Class<?>) custListType.getActualTypeArguments()[0];
        System.out.println(custListClass); // ?
    }

    class E extends Exception {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}