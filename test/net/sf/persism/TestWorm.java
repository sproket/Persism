package net.sf.persism;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

/**
 * Comments for TestThread go here.
 *
 * @author Dan Howard
 * @since 4/14/12 6:05 AM
 */
public class TestWorm {

    private static Map<String, Object> map = new HashMap<String, Object>(32);

    public Object getValue(String key) {

        LogManager man = LogManager.getLogManager();

        if (map.get(key) != null) {
            return map.get(key);
        }

        ConcurrentHashMap<String,Object> cm = new ConcurrentHashMap<String, Object>(32, 0.75f, 1);
//        cm.putIfAbsent();
//                cm.get()

//        Collections.singleton(key)
        // do some process to get Object
        Object o = new Object();
        map.put(key, o);
        return o;

    }

}
