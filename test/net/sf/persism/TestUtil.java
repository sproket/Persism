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
import net.sf.persism.dao.records.CustomerOrderRec;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
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
        text = Util.replaceAll(text, ' ', '_');
        log.info(text);
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

    public void testReflectionStuff() {
        Class<?> objectClass = CustomerOrderRec.class;

        Constructor<?>[] cons = objectClass.getConstructors();
        for (int i = 0; i < cons.length; i++) {
            log.info(i + " " + cons[i]);

            log.info("PARAMS: ");
            for (int k = 0; k < cons[i].getParameters().length; k++) {
                log.info("   " + k + " " + cons[i].getParameters()[k].getName());
            }

            if (cons[i].getAnnotation(ConstructorProperties.class) != null) {
                ConstructorProperties x = cons[i].getAnnotation(ConstructorProperties.class);
                for (int k = 0; k < x.value().length; k++) {
                    log.info("   " + k + " " + x.value()[k]);
                }
            } else {
                log.info("no ConstructorProperties?");
            }
        }

    }

}
