package net.sf.persism;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.*;
import java.sql.ResultSet;
import java.util.*;
import java.util.Date;

/**
 * Comments for Util go here.
 *
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

    public static <T> boolean isRecord(Class<T> objectClass) {
        // Java 8 test for isRecord since class.isRecord doesn't exist in Java 8
        Class<?> sup = objectClass.getSuperclass();
        while (!sup.equals(Object.class) ) {
            if ("java.lang.Record".equals(sup.getName())) {
                return true;
            }
            sup = sup.getSuperclass();
        }
        return false;
    }



}
