package net.sf.persism;

import junit.framework.TestCase;
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
}
