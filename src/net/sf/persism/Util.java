package net.sf.persism;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

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

    // Performs a possible conversion of types if there's a case
    // or leaves the original object alone.
    static Object convert(Object from, Types to) {
        assert from != null;
        assert to != null;

        if (from.getClass().isEnum() && to == Types.StringType || to == Types.CharacterType) {
            return "" + from;
        }

        if (from instanceof java.util.Date) {

            switch (to) {
                case TimestampType:
                    return new Timestamp(((Date) from).getTime());

                case LongType:
                    return ((Date) from).getTime();

                case SQLDateType:
                    return new java.sql.Date(((Date) from).getTime());
            }
        }

        if (from instanceof Long) {
            switch (to) {
                case TimestampType:
                    return new Timestamp((Long) from);

                case SQLDateType:
                    return new java.sql.Date((Long) from);

                case UtilDateType:
                    return new java.util.Date((Long) from);
            }

        }

        return from;
    }
    static void prepareParameters(PreparedStatement st, Object[] parameters) throws SQLException {
    }

    // Place code conversions here to prevent type exceptions on setObject
    static void setParameters(PreparedStatement st, Object[] parameters) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("PARAMS: " + Arrays.asList(parameters));
        }
        // todo did we not have a test for Character? FFS.
        int n = 1;
        for (Object o : parameters) {
            if (o instanceof UUID) {
                st.setString(n, o.toString());
            } else {
                st.setObject(n, o);
            }
            n++;
        }
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


    public static String getProperty(String key) {
        Properties properties = new Properties();
        URL resource = Util.class.getResource("/persism.properties");
        if (resource != null) {
            try {
                InputStream is = resource.openStream();
                properties.load(is);
                is.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return properties.getProperty(key);
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
}
