/**
 * Comments for TestUtil go here.
 *
 * @author Dan Howard
 * @since 5/24/12 5:33 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.Invoice;
import net.sf.persism.dao.Postman;
import org.junit.Test;

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
            if (str.equals("b")) return; // only skips this iteration.

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

        System.out.println((int)x);
        System.out.println((int)y);
        System.out.println((int)z);
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

    class E extends  Exception {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}