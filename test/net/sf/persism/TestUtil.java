/**
 * Comments for TestUtil go here.
 *
 * @author Dan Howard
 * @since 5/24/12 5:33 PM
 */
package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.Postman;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

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

    public void testReflections() throws Exception {
//        List<ClassLoader> classLoadersList = new LinkedList<>();
//        classLoadersList.add(ClasspathHelper.contextClassLoader());
//        classLoadersList.add(ClasspathHelper.staticClassLoader());
//
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
//                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
//                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("net.sf.persism.dao"))));
//
//        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        //log.warn(classes.toString());

        //log.warn(getClassesForPackage("net.sf.persism.dao").toString());
        log.warn(Arrays.asList(getClasses("net.sf.persism.dao")));
    }


    /**
     * Scans all classloaders for the current thread for loaded jars, and then scans
     * each jar for the package name in question, listing all classes directly under
     * the package name in question. Assumes directory structure in jar file and class
     * package naming follow java conventions (i.e. com.example.test.MyTest would be in
     * /com/example/test/MyTest.class)
     */
    public Collection<Class> getClassesForPackage(String packageName) throws Exception {
        String packagePath = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Set<URL> jarUrls = new HashSet<URL>();

        Class<?> sup = classLoader.getClass().getSuperclass();
        while (!sup.equals(Object.class) ) {
            if ("java.lang.ClassLoader".equals(sup.getName())) {
                break;
            }
            sup = sup.getSuperclass();
        }


log.warn(sup.getName());
        Field f = sup.getField("classes");
        log.warn(f);
//        Method m = classLoader.getClass().getMethod("getClasses");
//        log.warn(m);

        // may want better way to detect jar files
        while (classLoader != null) {

            if (classLoader instanceof URLClassLoader) {

                for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                    log.info(url.getFile());
//                    if (url.getFile().endsWith(".jar")) {
                    jarUrls.add(url);
//                    }
                }
            }


            classLoader = classLoader.getParent();
        }

        Set<Class> classes = new HashSet<Class>();

        for (URL url : jarUrls) {
            JarInputStream stream = new JarInputStream(url.openStream()); // may want better way to open url connections
            JarEntry entry = stream.getNextJarEntry();

            while (entry != null) {
                String name = entry.getName();
                int i = name.lastIndexOf("/");

                if (i > 0 && name.endsWith(".class") && name.substring(0, i).equals(packagePath))
                    classes.add(Class.forName(name.substring(0, name.length() - 6).replace("/", ".")));

                entry = stream.getNextJarEntry();
            }

            stream.close();
        }

        return classes;
    }

// https://stackoverflow.com/questions/1156552/java-package-introspection
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}

