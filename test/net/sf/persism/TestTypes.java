/**
 * Comments for TestTypes go here.
 * @author Dan Howard
 * @since 10/8/11 5:44 PM
 */
package net.sf.persism;

import junit.framework.TestCase;

public class TestTypes extends TestCase {

    private static final Log log = Log.getLogger(TestTypes.class);


    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // todo We have a general test types in H2, SQLite and Derby - Move to base and remove this class
    public void testSomething() {
        try {
            log.info(boolean.class.getName());
            log.info(Boolean.class.getName());
            log.info(boolean.class == Boolean.class);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }
}
