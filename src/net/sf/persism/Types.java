package net.sf.persism;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Type mapper used for fast switch statements in getTypedValue method,
 *
 * @author Dan Howard
 * @since 10/8/11 5:36 PM
 */
enum Types {

    booleanType(boolean.class),
    BooleanType(Boolean.class),
    byteType(byte.class),
    ByteType(Byte.class),
    shortType(short.class),
    ShortType(Short.class),
    integerType(int.class),
    IntegerType(Integer.class),
    longType(long.class),
    LongType(Long.class),
    floatType(float.class),
    FloatType(Float.class),
    doubleType(double.class),
    DoubleType(Double.class),
    DecimalType(BigDecimal.class),
    StringType(String.class),
    characterType(char.class),
    CharacterType(Character.class),
    UtilDateType(java.util.Date.class),
    SQLDateType(java.sql.Date.class),
    TimeType(Time.class),
    TimestampType(Timestamp.class),
    LocalDate(java.time.LocalDate.class),
    LocalDateTime(java.time.LocalDateTime.class),
    Instant(java.time.Instant.class),
    OffsetDateTime(java.time.OffsetDateTime.class),
    ZonedDateTime(java.time.ZonedDateTime.class),
    byteArrayType(byte[].class),
    ByteArrayType(Byte[].class),
    ClobType(Clob.class),
    BlobType(Blob.class),
    EnumType(Enum.class),
    UUIDType(UUID.class);

    private static final Log log = Log.getLogger(Types.class);

    private Class type;

    <T> Types(Class<T> type) {
        init(type);
    }

    private <T> void init(Class<T> type) {
        this.type = type;
    }

    public static <T> Types getType(Class<T> type) {
        for (Types t : values()) {
            if (t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

    public static Types convert(int sqlType) {
        Types result = null;

        switch (sqlType) {
            case java.sql.Types.CHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                result = StringType;
                break;

            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                result = DecimalType;
                break;

            case java.sql.Types.BIT:
            case java.sql.Types.BOOLEAN:
                result = BooleanType;
                break;

            case java.sql.Types.TINYINT:
                result = ByteType;
                break;

            case java.sql.Types.SMALLINT:
                result = ShortType;
                break;

            case java.sql.Types.INTEGER:
                result = IntegerType;
                break;

            case java.sql.Types.BIGINT:
                result = LongType;
                break;

            case java.sql.Types.FLOAT:
                result = FloatType;
                break;

            case java.sql.Types.DOUBLE:
            case java.sql.Types.REAL:
                result = DoubleType;
                break;

            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.BLOB:
                result = BlobType;
                break;

            case java.sql.Types.DATE:
                result = SQLDateType;
                break;

            case java.sql.Types.TIME:
                result = TimeType;
                break;

            case java.sql.Types.TIMESTAMP:
                result = TimestampType;
                break;

            case java.sql.Types.CLOB:
                result = ClobType;
                break;
        }

        if (result == null) {
            // Need this for converter
            // https://stackoverflow.com/questions/36405320/using-the-datetimeoffset-datatype-with-jtds
            if (sqlType == -155) {
                // MSSQL type for DateTimeOffset
                result = TimestampType;
            } else {
                log.warn("SQL TYPE: " + sqlType + " not found ", new Throwable());
            }
        }

        return result;
    }

    public boolean isCountable() {
        return this == IntegerType || this == integerType || this == LongType || this == longType || this == byteType || this == ByteType
                || this == ShortType || this == shortType || this == DoubleType || this == doubleType || this == DecimalType;
    }
}


