package net.sf.persism;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

/**
 * @author Dan Howard
 * @since 9/25/11 3:54 PM
 */
public class UtilsForTests {

    private static final Log log = Log.getLogger(UtilsForTests.class);

    private UtilsForTests() {
    }

    public static String replace(String baseStr, String strOld, String strNew) {
        // Takes care of possible /x characters in the string's replaceAll function.
        if (null == baseStr || baseStr.length() == 0) {
            return "";
        }
        StringBuilder lsNewStr = new StringBuilder(baseStr.length());

        int liFound;
        int liLastPointer = 0;

        do {
            liFound = baseStr.indexOf(strOld, liLastPointer);

            if (liFound < 0) {
                lsNewStr.append(baseStr.substring(liLastPointer));
            } else {

                if (liFound > liLastPointer) {
                    lsNewStr.append(baseStr, liLastPointer, liFound);
                }

                lsNewStr.append(strNew);
                liLastPointer = liFound + strOld.length();
            }

        } while (liFound > -1);

        return lsNewStr.toString();
    }

    private static final String[] tableType = {"TABLE"};
    private static final String[] viewType = {"VIEW"};

    public static boolean isTableInDatabase(String tableName, Connection con) throws SQLException {
        boolean result = false;
        DatabaseMetaData dma = con.getMetaData();
        try (ResultSet rs = dma.getTables(null, null, null, tableType)) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isTableInDatabase(String schema, String tableName, Connection con) throws SQLException {
        boolean result = false;
        DatabaseMetaData dma = con.getMetaData();
        try (ResultSet rs = dma.getTables(null, schema, null, tableType)) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isViewInDatabase(String viewName, Connection con) throws SQLException {
        boolean result = false;
        DatabaseMetaData dma = con.getMetaData();
        try (ResultSet rs = dma.getTables(null, null, null, viewType)) {
            while (rs.next()) {
                if (viewName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isProcedureInDatabase(String procName, Connection con) throws SQLException {
        boolean result = false;
        DatabaseMetaData dma = con.getMetaData();
        try (ResultSet rs = dma.getProcedures(null, null, "%")) {
            while (rs.next()) {
                String proc = rs.getString("PROCEDURE_NAME");
                // looks like spCustomerOrders;1 == where ;1 indicate # params
                if (proc != null && proc.toLowerCase().startsWith(procName.toLowerCase())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isFieldInTable(String field, String table, Connection con) throws SQLException {
        boolean result = false;
        DatabaseMetaData dma = con.getMetaData();
        try (ResultSet rs = dma.getColumns(null, null, table, null)) {
            String c;
            while (rs.next()) {
                c = rs.getString("COLUMN_NAME").toUpperCase();
                if (field.toUpperCase().compareTo(c) == 0) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

    public static String createHomeFolder(String subFolder) {

        String home = System.getProperty("user.home");
        home = replace(home, "\\", "/");
        home += "/" + subFolder;


        log.info("createHomeFolder: " + home);
        boolean success = new File(home).mkdirs();
        log.info("createHomeFolder success: " + success);
        return home;
    }


    public static void cleanup(Statement st, java.sql.ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

//    public static void createTable(TableDef table, Connection con) throws SQLException {
//        StringBuilder sb = new StringBuilder();
//        sb.append("CREATE TABLE ").append(table.getName()).append(" (");
//        List<FieldDef> fields = table.getFields();
//        String sep = "";
//        for (FieldDef field : fields) {
//
//            JavaType type = JavaType.getType(field.getType());
//            String sqlType = null;
//            switch (type) {
//
//                case booleanType:
//                case BooleanType:
//                    sqlType = "BIT";
//                    break;
//
//                case byteType:
//                case ByteType:
//                    break;
//
//                case shortType:
//                case ShortType:
//                    break;
//
//                case integerType:
//                case IntegerType:
//                    sqlType = "INT";
//                    break;
//
//                case longType:
//                case LongType:
//                    sqlType = "NUMBER(" + field.getLength() + ")";
//                    break;
//
//                case floatType:
//                case FloatType:
//                    sqlType = "NUMBER(" + field.getLength() + "," + field.getScale() + ")";
//                    break;
//
//                case doubleType:
//                case DoubleType:
//                    sqlType = "NUMBER(" + field.getLength() + "," + field.getScale() + ")";
//                    break;
//
//                case BigDecimalType:
//                    sqlType = "NUMBER(" + field.getLength() + "," + field.getScale() + ")";
//                    break;
//
//                case StringType:
//                    sqlType = "VARCHAR(" + field.getLength() + ")";
//                    break;
//
//                case characterType:
//                case CharacterType:
//                    sqlType = "CHAR(" + field.getLength() + ")";
//                    break;
//
//                case UtilDateType:
//                case SQLDateType:
//                    sqlType = "DATE"; // DATETIME IN SQL SERVER?
//                    break;
//
//                case TimeType:
//                    break;
//
//                case TimestampType:
//                    sqlType = "TIMESTAMP";
//                    break;
//
//                case byteArrayType:
//                case ByteArrayType:
//                    break;
//
//                case ClobType:
//                    break;
//
//                case BlobType:
//                    break;
//            }
//
//            sb.append(sep).append(field.getName()).append(" ").append(sqlType);
//            sep = ", ";
//        }
//        sb.append(") ");
//
//
//        log.info(sb.toString());
//
//        Statement st = null;
//        try {
//            st = con.createStatement();
//            if (isTableInDatabase(table.getName(), con)) {
//                st.execute("DROP TABLE " + table.getName());
//            }
//            st.execute(sb.toString());
//        } finally {
//            cleanup(st, null);
//        }
//
//    }


    // YYYYMMDDhhmmss          yyyyMMDDhhmmss ?
    public static Calendar getCalendarFromAnsiDateString(String dateString) {

        Calendar cal = Calendar.getInstance();

        int year, month, day, hour, min, sec;
        year = Integer.parseInt(dateString.substring(0, 4));
        month = Integer.parseInt(dateString.substring(4, 6)) - 1;
        day = Integer.parseInt(dateString.substring(6, 8));
        hour = 0;
        min = 0;
        sec = 0;

        if (dateString.length() > 8) {
            hour = Integer.parseInt(dateString.substring(8, 10));
            if (dateString.length() > 10) {
                min = Integer.parseInt(dateString.substring(10, 12));

                if (dateString.length() > 12) {
                    sec = Integer.parseInt(dateString.substring(12, 14));
                }
            }

        }
        cal.clear();
        cal.set(year, month, day, hour, min, sec);

        return cal;
    }

    public static String readFromResource(String path) {
        try (InputStream is = UtilsForTests.class.getResourceAsStream(path)) {
            return new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}
