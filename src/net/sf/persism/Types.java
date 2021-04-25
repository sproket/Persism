package net.sf.persism;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Type mapper used for fast switch statements
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
    BigDecimalType(BigDecimal.class),
    BigIntegerType(BigInteger.class),
    StringType(String.class),
    characterType(char.class),
    CharacterType(Character.class),
    UtilDateType(java.util.Date.class),
    SQLDateType(java.sql.Date.class),
    TimeType(Time.class),
    LocalTimeType(LocalTime.class),
    TimestampType(Timestamp.class),
    LocalDateType(java.time.LocalDate.class),
    LocalDateTimeType(java.time.LocalDateTime.class),
    InstantType(java.time.Instant.class),
    OffsetDateTimeType(java.time.OffsetDateTime.class),
    ZonedDateTimeType(java.time.ZonedDateTime.class),
    byteArrayType(byte[].class),
    ByteArrayType(Byte[].class),
    ClobType(Clob.class),
    BlobType(Blob.class),
    EnumType(Enum.class),
    UUIDType(UUID.class),
    ObjectType(Object.class);

    private static final Log log = Log.getLogger(Types.class);

    private Class<?> type;

    <T> Types(Class<T> type) {
        init(type);
    }

    private <T> void init(Class<T> type) {
        this.type = type;
    }

    public static <T> Types getType(Class<T> type) {
        if (type.isEnum()) {
            return EnumType;
        }

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
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                result = StringType;
                break;

            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                result = BigDecimalType;
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
                result = ByteArrayType;
                break;

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
            case java.sql.Types.NCLOB:
                result = ClobType;
                break;

            case java.sql.Types.OTHER:
                result = ObjectType;
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

    public Class<?> getJavaType() {
        return type;
    }

    public boolean isEligibleForAutoinc() {
        // Oracle returns BigDecimalType for INT
        // PUBS has SmallInt -> short
        return this == IntegerType || this == integerType || this == LongType || this == longType
                || this == ShortType || this == shortType || this == BigDecimalType || this == BigIntegerType;
    }

    // https://stackoverflow.com/questions/2891970/getting-default-value-for-primitive-types
    public static <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0);
    }
}


