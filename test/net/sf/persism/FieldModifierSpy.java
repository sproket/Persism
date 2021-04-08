package net.sf.persism;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static java.lang.System.out;

enum Spy { BLACK , WHITE }

// https://docs.oracle.com/javase/tutorial/reflect/member/fieldModifiers.html
public class FieldModifierSpy {

    volatile int share;
    int instance;
    class Inner {}

    public static void spy(Class<?> c, String... modifierNames ) {

        try {
            int searchMods = 0x0;
            for (int i = 0; i < modifierNames.length; i++) {
                searchMods |= modifierFromString(modifierNames[i]);
            }

            Field[] flds = c.getDeclaredFields();
            out.format("Fields in Class '%s' containing modifiers:  %s%n",
                    c.getName(),
                    Modifier.toString(searchMods));
            boolean found = false;
            for (Field f : flds) {
                int foundMods = f.getModifiers();
                // Require all of the requested modifiers to be present
                if ((foundMods & searchMods) == searchMods) {
                    out.format("%-8s [ synthetic=%-5b enum_constant=%-5b ]%n",
                            f.getName(), f.isSynthetic(),
                            f.isEnumConstant());
                    found = true;
                }
            }

            if (!found) {
                out.format("No matching fields%n");
            }

            // production code should handle this exception more gracefully
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int modifierFromString(String s) {
        int m = 0x0;
        if ("public".equals(s))           m |= Modifier.PUBLIC;
        else if ("protected".equals(s))   m |= Modifier.PROTECTED;
        else if ("private".equals(s))     m |= Modifier.PRIVATE;
        else if ("static".equals(s))      m |= Modifier.STATIC;
        else if ("final".equals(s))       m |= Modifier.FINAL;
        else if ("transient".equals(s))   m |= Modifier.TRANSIENT;
        else if ("volatile".equals(s))    m |= Modifier.VOLATILE;
        return m;
    }

}
