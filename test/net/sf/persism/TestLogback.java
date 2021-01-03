/**
 * Comments for TestLogback go here.
 * @author Dan Howard
 * @since 4/22/12 8:35 AM
 */
package net.sf.persism;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogback extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(TestLogback.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSomething() {
        try {
            //@todo write testcase for TestLogback
            throw new Exception("COW");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }
}
