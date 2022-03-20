package net.sf.persism;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

final class Converter {

    private static final Log log = Log.getLogger(Converter.class);

    // https://stackoverflow.com/questions/2409657/call-to-method-of-static-java-text-dateformat-not-advisable
    // https://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT1 =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT2 =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT3 =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("hh:mm:ss"));

    // Make a sensible conversion of the value type from the DB and the property type defined
    // on the Data class - or the value type from the property to the statement parameter.
    Object convert(Object value, Class<?> targetType, String columnName) {
        assert value != null;

        Types valueType = Types.getType(value.getClass());

        if (valueType == null) {
            log.warn(Messages.NoConversionForUnknownType.message(value.getClass()));
            return value;
        }

        Object returnValue = value;

        // try to convert or cast (no cast) the value to the proper type.
        switch (valueType) {

            case booleanType:
            case BooleanType:
                break;

            case byteType:
            case ByteType:
                log.warnNoDuplicates(Messages.TinyIntMSSQL.message(columnName));
                break;

            case shortType:
            case ShortType:
                break;

            case integerType:
            case IntegerType:

                if (targetType == Integer.class || targetType == int.class) {
                    break;
                }

                // int to bool
                if (targetType == Boolean.class || targetType == boolean.class) {
                    returnValue = Integer.parseInt("" + value) != 0;
                    break;
                }

                if (targetType == Short.class || targetType == short.class) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "SHORT", "INT"));
                    returnValue = Short.parseShort("" + value);
                    break;
                }

                if (targetType == Byte.class || targetType == byte.class) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "BYTE", "INT"));
                    returnValue = Byte.parseByte("" + value);
                    break;
                }

                if (targetType == Time.class) {
                    // SQLite when a Time is defined VIA a convert from LocalTime via Time.valueOf (see getContactForTest)
                    returnValue = new Time(((Integer) value).longValue());
                    break;
                }

                if (targetType == LocalTime.class) {
                    // SQLite for Time SQLite sees Long, for LocalTime it sees Integer
                    returnValue = new Time((Integer) value).toLocalTime();
                    break;
                }

                break;

            case longType:
            case LongType:

                if (targetType == Long.class || targetType == long.class) {
                    break;
                }

                long lval = Long.parseLong("" + value);
                if (targetType == java.sql.Date.class) {
                    returnValue = new java.sql.Date(lval);
                    break;
                }

                if (targetType == Date.class) {
                    returnValue = new java.util.Date(lval);
                    break;
                }

                if (targetType == Timestamp.class) {
                    returnValue = new Timestamp(lval);
                    break;
                }

                if (targetType == Integer.class || targetType == int.class) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "INT", "LONG"));
                    returnValue = Integer.parseInt("" + lval);
                    break;
                }

                if (targetType == LocalDateTime.class) {
                    returnValue = new Timestamp(lval).toLocalDateTime();
                    break;
                }

                if (targetType == LocalDate.class) {
                    // SQLite reads long as date.....
                    returnValue = new Timestamp(lval).toLocalDateTime().toLocalDate();
                    break;
                }

                if (targetType == Time.class) {
                    // SQLite.... Again.....
                    returnValue = new Time((Long) value);
                    break;
                }
                break;

            case floatType:
            case FloatType:
                break;

            case doubleType:
            case DoubleType:
                if (targetType == Double.class || targetType == double.class) {
                    break;
                }

                Double dbl = (Double) value;
                // float or doubles to BigDecimal
                if (targetType == BigDecimal.class) {
                    returnValue = new BigDecimal("" + value);
                    break;
                }

                if (targetType == Float.class || targetType == float.class) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "FLOAT", "DOUBLE"));
                    returnValue = dbl.floatValue();
                    break;
                }

                if (targetType == Integer.class || targetType == int.class) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "INT", "DOUBLE"));
                    returnValue = dbl.intValue();
                    break;
                }

                if (targetType == Long.class || targetType == long.class) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "LONG", "DOUBLE"));
                    returnValue = dbl.longValue();
                    break;
                }
                break;

            case BigDecimalType:
                if (targetType == BigDecimal.class) {
                    break;
                }

                if (targetType == Float.class || targetType == float.class) {
                    returnValue = ((Number) value).floatValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "FLOAT", "BigDecimal"));
                    break;
                }

                if (targetType == Double.class || targetType == double.class) {
                    returnValue = ((Number) value).doubleValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "DOUBLE", "BigDecimal"));
                    break;
                }

                if (targetType == Long.class || targetType == long.class) {
                    returnValue = ((Number) value).longValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "LONG", "BigDecimal"));
                    break;
                }

                if (targetType == String.class) {
                    returnValue = (value).toString();
                    break;
                }

                if (targetType == Integer.class || targetType == int.class) {
                    returnValue = ((Number) value).intValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "INT", "BigDecimal"));
                    break;
                }

                if (targetType == Short.class || targetType == short.class) {
                    returnValue = ((Number) value).shortValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "SHORT", "BigDecimal"));
                    break;
                }

                if (targetType == Boolean.class || targetType == boolean.class) {
                    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
                    returnValue = ((Number) value).intValue() == 1;
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "BOOLEAN", "BigDecimal"));
                    break;
                }

                break;

            case StringType:
                if (targetType == String.class) {
                    break;
                }

                java.util.Date dval;
                DateFormat df;
                if (("" + value).length() > "yyyy-MM-dd".length()) {
                    df = DATE_FORMAT1.get();
                } else {
                    df = DATE_FORMAT2.get();
                }

                // Read a string but we want a date
                if (targetType == Date.class || targetType == java.sql.Date.class) {
                    // This condition occurs in SQLite when you have a datetime with default annotated
                    // the format returned is 2012-06-02 19:59:49
                    // Used for SQLite returning dates as Strings under some conditions
                    // SQL or others may return STRING yyyy-MM-dd for older legacy 'date' type.
                    // https://docs.microsoft.com/en-us/sql/t-sql/data-types/date-transact-sql?view=sql-server-ver15
                    dval = tryParseDate(value, targetType, columnName, df);

                    if (targetType == java.sql.Date.class) {
                        // does not occur. SQLite sees sql-date as Long, so we never do this one
                        returnValue = new java.sql.Date(dval.getTime());
                    } else {
                        returnValue = dval;
                    }
                    break;
                }

                if (targetType == Timestamp.class) {
                    returnValue = tryParseTimestamp(value, targetType, columnName);
                    break;
                }

                if (targetType == LocalDate.class) {
                    // JTDS
                    dval = tryParseDate(value, targetType, columnName, df);
                    returnValue = dval.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    break;
                }

                if (targetType == LocalDateTime.class) {
                    // JTDS
                    dval = tryParseDate(value, targetType, columnName, df);
                    returnValue = dval.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    break;
                }

                if (targetType.isEnum()) {
                    // If this is an enum do a case-insensitive comparison
                    Object[] enumConstants = targetType.getEnumConstants();
                    for (Object element : enumConstants) {
                        if (("" + value).equalsIgnoreCase(element.toString())) {
                            returnValue = element;
                            break;
                        }
                    }
                    break;
                }

                if (targetType == UUID.class) {
                    returnValue = UUID.fromString("" + value);
                    break;
                }

                if (targetType == Boolean.class || targetType == boolean.class) {
                    // String to Boolean - T or 1 - otherwise false (or null)
                    String bval = ("" + value).toUpperCase();
                    returnValue = bval.startsWith("T") || bval.startsWith("1");
                    break;
                }

                if (targetType == Time.class) {
                    // MSSQL works, JTDS returns Varchar in format below with varying decimal numbers
                    // which won't format unless I use Exact, so I chop of the milliseconds.
                    // This case only occurs with JTDS which is no longer supported
                    DateFormat timeFormat = DATE_FORMAT3.get();
                    String sval = "" + value;
                    if (sval.indexOf('.') > -1) {
                        sval = sval.substring(0, sval.indexOf('.'));
                    }
                    dval = tryParseDate(sval, targetType, columnName, timeFormat);
                    returnValue = new Time(dval.getTime());
                    break;
                }

                if (targetType == LocalTime.class) {
                    // JTDS Fails again... and is no longer supported
                    returnValue = LocalTime.parse("" + value);
                    break;
                }

                if (targetType == BigDecimal.class) {
                    try {
                        returnValue = new BigDecimal("" + value);
                    } catch (NumberFormatException e) {
                        throw new PersismException(Messages.NumberFormatException.message(columnName, targetType, value.getClass(), value), e);
                    }
                    break;
                }
                break;

            case characterType:
            case CharacterType:
                break;

            case LocalDateType:
                returnValue = java.sql.Date.valueOf((LocalDate) value);
                break;

            case LocalDateTimeType:
                returnValue = Timestamp.valueOf((LocalDateTime) value);
                break;

            case LocalTimeType:
                returnValue = Time.valueOf((LocalTime) value);
                break;

            case UtilDateType:
                if (targetType == Date.class) {
                    break;
                }

                if (targetType == java.sql.Date.class) {
                    returnValue = new java.sql.Date(((Date) value).getTime());
                    break;
                }

                if (targetType == Timestamp.class) {
                    returnValue = new Timestamp(((Date) value).getTime());
                    break;
                }

                if (targetType == LocalDate.class) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    break;
                }

                if (targetType == LocalDateTime.class) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    break;
                }

                if (targetType == Time.class) {
                    // Oracle doesn't seem to have Time so we use Timestamp
                    returnValue = new Time(((Date) value).getTime());
                    break;
                }

                if (targetType == LocalTime.class) {
                    // Oracle.... Sigh
                    Date dt = (Date) value;
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
                    break;
                }
                break;

            case SQLDateType:
                if (targetType == java.sql.Date.class) {
                    break;
                }

                if (targetType == Date.class) {
                    returnValue = new java.util.Date(((Date) value).getTime());
                    break;
                }

                if (targetType == Timestamp.class) {
                    returnValue = new Timestamp(((Date) value).getTime());
                    break;
                }

                if (targetType == LocalDate.class) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    break;
                }

                if (targetType == LocalDateTime.class) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    break;
                }

                if (targetType == Time.class) {
                    // Oracle doesn't seem to have Time so we use Timestamp
                    returnValue = new Time(((Date) value).getTime());
                    break;
                }

                if (targetType == LocalTime.class) {
                    // Oracle.... Sigh
                    Date dt = (Date) value;
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
                    break;
                }
                break;

            case TimestampType:
                if (targetType == Timestamp.class) {
                    break;
                }

                if (targetType == Date.class) {
                    returnValue = new java.util.Date(((Date) value).getTime());
                    break;
                }

                if (targetType == java.sql.Date.class) {
                    returnValue = new java.sql.Date(((Date) value).getTime());
                    break;
                }

                if (targetType == LocalDate.class) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    break;
                }

                if (targetType == LocalDateTime.class) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    break;
                }

                if (targetType == Time.class) {
                    // Oracle doesn't seem to have Time so we use Timestamp
                    returnValue = new Time(((Date) value).getTime());
                    break;
                }

                if (targetType == LocalTime.class) {
                    // Oracle.... Sigh
                    Date dt = (Date) value;
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
                    break;
                }
                break;

            case TimeType:
                if (targetType == LocalTime.class) {
                    returnValue = LocalTime.parse("" + value);
                }
                break;

            case InstantType:
            case OffsetDateTimeType:
            case ZonedDateTimeType:
                log.warn(Messages.ConverterValueTypeNotYetSupported.message(valueType.getJavaType()), new Throwable());
                break;

            case byteArrayType:
            case ByteArrayType:
                if (targetType == UUID.class) {
                    returnValue = asUuid((byte[]) value);
                }
                break;

            case ClobType:
            case BlobType:
                log.warn(Messages.ConverterDoNotUseClobOrBlobAsAPropertyType.message(), new Throwable());
                break;

            case EnumType:
                // No need to convert it here.
                // If it's being used for the property setter then it's OK
                // If it's being used by setParameters it's converted to String
                // The String case above converts from the String to the Enum
                log.debug("EnumType");
                break;

            case UUIDType:
                if (targetType == Blob.class || targetType == byte[].class || targetType == Byte[].class) {
                    returnValue = asBytes((UUID) value);
                }
                break;

            case ObjectType:
                break;
        }
        return returnValue;
    }

    /*
     * Used by convert for convenience - common possible parsing
     */
    static Date tryParseDate(Object value, Class<?> targetType, String columnName, DateFormat df) throws PersismException {
        try {
            return df.parse("" + value);
        } catch (ParseException e) {
            throw new PersismException(Messages.DateFormatException.message(e.getMessage(), columnName, targetType, value.getClass(), value), e);
        }
    }

    static Timestamp tryParseTimestamp(Object value, Class<?> targetType, String columnName) throws PersismException {
        try {
            return Timestamp.valueOf("" + value);
        } catch (IllegalArgumentException e) {
            throw new PersismException(Messages.DateFormatException.message(e.getMessage(), columnName, targetType, value.getClass(), value), e);
        }
    }

    // todo document UUID usage for supported and non-supported DBs. Point out this URL to get converter code - otherwise we could expose these 2 static methods somewhere (which we dont want to do)
    // https://stackoverflow.com/questions/17893609/convert-uuid-to-byte-that-works-when-using-uuid-nameuuidfrombytesb
    // THANKS!
    static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }


}
