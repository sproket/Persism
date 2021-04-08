package net.sf.persism;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dan Howard
 * @since 4/1/12 6:48 AM
 */
final class Util {

    private static final Log log = Log.getLogger(Util.class);

    private Util() {
    }


    static void rollback(Connection con) {
        try {
            if (con != null && !con.getAutoCommit()) {
                con.rollback();
            }
        } catch (SQLException e1) {
            log.error(e1.getMessage(), e1);
        }

    }

    static void cleanup(Statement st, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    static void cleanup(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static boolean containsColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException sqlex) {
        }
        return false;
    }

    public static String camelToTitleCase(String text) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i == 0) {
                sb.append(c);
            } else {
                if (Character.isUpperCase(c)) {
                    sb.append(" ");
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String replaceAll(String text, char from, char to) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == from) {
                sb.append(to);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection/31268945#31268945
    // https://stackoverflow.com/questions/56039341/get-declared-fields-of-java-lang-reflect-fields-in-jdk12/56042394#56042394
    // https://docs.oracle.com/javase/tutorial/reflect/member/fieldModifiers.html
//    private static final VarHandle MODIFIERS;
//    static {
//        try {
//            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
//            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
//        } catch (IllegalAccessException | NoSuchFieldException ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    public static void setFieldValue(Field field, Object onObject, Object value) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        VarHandle MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);

        int mods = field.getModifiers();
        if (Modifier.isFinal(mods)) {
            MODIFIERS.set(field, mods & ~Modifier.FINAL);
        }

        field.set(onObject, value);
//        MODIFIERS.set(field, mods);
//        field.setAccessible(false);
    }

    // this is getting redonculous. WAY TOO MUCH REFLECTION
    public static void updateFields(Object source, Object dest) throws IllegalAccessException, NoSuchFieldException {
        assert source != null;
        assert dest != null;
        assert source.getClass().equals(dest.getClass());

        List<Field> fields = new ArrayList<>(32);

        // getDeclaredFields does not get fields from super classes.....
        fields.addAll(Arrays.asList(source.getClass().getDeclaredFields()));
        Class<?> sup = source.getClass().getSuperclass();
        log.debug("fields for %s", sup);
        while (!sup.equals(Object.class) && !sup.equals(PersistableObject.class)) {
            fields.addAll(Arrays.asList(sup.getDeclaredFields()));
            sup = sup.getSuperclass();
            log.debug("fields for %s", sup);
        }

        for (Field field: fields) {
            field.setAccessible(true);
            Object value = field.get(source);
            setFieldValue(field, dest, value);
//            field.setAccessible(false);
        }

    }
}
