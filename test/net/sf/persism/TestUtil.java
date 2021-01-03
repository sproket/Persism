/**
 * Comments for TestUtil go here.
 * @author Dan Howard
 * @since 5/24/12 5:33 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.Postman;

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
        text = Util.replaceAll(text,' ', '_');
        log.info(text);
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
    }
}
