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
import java.util.*;

final class Converter {

    private static final Log log = Log.getLogger(Converter.class);

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
                // int to bool
                if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                    returnValue = Integer.parseInt("" + value) != 0;

                } else if (targetType.equals(Time.class)) {
                    // SQLite when a Time is defined VIA a convert from LocalTime via Time.valueOf (see getContactForTest)
                    returnValue = new Time(((Integer) value).longValue());

                } else if (targetType.equals(LocalTime.class)) {
                    // SQLite for Time SQLite sees Long, for LocalTime it sees Integer
                    returnValue = new Time((Integer) value).toLocalTime();

                } else if (targetType.equals(Short.class) || targetType.equals(short.class)) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "SHORT", "INT"));
                    returnValue = Short.parseShort("" + value);

                } else if (targetType.equals(Byte.class) || targetType.equals(byte.class)) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "BYTE", "INT"));
                    returnValue = Byte.parseByte("" + value);
                }
                break;

            case longType:
            case LongType:
                long lval = Long.parseLong("" + value);
                if (targetType.equals(java.sql.Date.class)) {
                    returnValue = new java.sql.Date(lval);

                } else if (targetType.equals(java.util.Date.class)) {
                    returnValue = new java.util.Date(lval);

                } else if (targetType.equals(Timestamp.class)) {
                    returnValue = new Timestamp(lval);

                } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "INT", "LONG"));
                    returnValue = Integer.parseInt("" + lval);

                } else if (targetType.equals(LocalDate.class)) {
                    // SQLite reads long as date.....
                    returnValue = new Timestamp(lval).toLocalDateTime().toLocalDate();

                } else if (targetType.equals(LocalDateTime.class)) {
                    returnValue = new Timestamp(lval).toLocalDateTime();

                } else if (targetType.equals(Time.class)) {
                    // SQLite.... Again.....
                    returnValue = new Time((Long) value);
                }
                break;

            case floatType:
            case FloatType:
                break;

            case doubleType:
            case DoubleType:
                Double dbl = (Double) value;
                // float or doubles to BigDecimal
                if (targetType.equals(BigDecimal.class)) {
                    returnValue = new BigDecimal("" + value);

                } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "FLOAT", "DOUBLE"));
                    returnValue = dbl.floatValue();

                } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "INT", "DOUBLE"));
                    returnValue = dbl.intValue();

                } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "LONG", "DOUBLE"));
                    returnValue = dbl.longValue();
                }
                break;

            case BigDecimalType:
                if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                    returnValue = ((Number) value).floatValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "FLOAT", "BigDecimal"));

                } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                    returnValue = ((Number) value).doubleValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "DOUBLE", "BigDecimal"));

                } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                    returnValue = ((Number) value).longValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "LONG", "BigDecimal"));

                } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                    returnValue = ((Number) value).intValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "INT", "BigDecimal"));

                } else if (targetType.equals(Short.class) || targetType.equals(short.class)) {
                    returnValue = ((Number) value).shortValue();
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "SHORT", "BigDecimal"));

                } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
                    returnValue = ((Number) value).intValue() == 1;
                    log.warnNoDuplicates(Messages.PossibleOverflow.message(columnName, "BOOLEAN", "BigDecimal"));

                } else if (targetType.equals(String.class)) {
                    returnValue = (value).toString();
                }
                break;

            case StringType:
                java.util.Date dval;
                String format;
                if (("" + value).length() > "yyyy-MM-dd".length()) {
                    format = "yyyy-MM-dd hh:mm:ss";
                } else {
                    format = "yyyy-MM-dd";
                }
                DateFormat df = new SimpleDateFormat(format);

                // Read a string but we want a date
                if (targetType.equals(java.util.Date.class) || targetType.equals(java.sql.Date.class)) {
                    // This condition occurs in SQLite when you have a datetime with default annotated
                    // the format returned is 2012-06-02 19:59:49
                    // Used for SQLite returning dates as Strings under some conditions
                    // SQL or others may return STRING yyyy-MM-dd for older legacy 'date' type.
                    // https://docs.microsoft.com/en-us/sql/t-sql/data-types/date-transact-sql?view=sql-server-ver15
                    dval = tryParseDate(value, targetType, columnName, df);

                    if (targetType.equals(java.sql.Date.class)) {
                        returnValue = new java.sql.Date(dval.getTime());
                    } else {
                        returnValue = dval;
                    }

                } else if (targetType.equals(Timestamp.class)) {
                    returnValue = tryParseTimestamp(value, targetType, columnName);

                } else if (targetType.equals(LocalDate.class)) {
                    // JTDS
                    dval = tryParseDate(value, targetType, columnName, df);
                    returnValue = dval.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                } else if (targetType.equals(LocalDateTime.class)) {
                    // JTDS
                    dval = tryParseDate(value, targetType, columnName, df);
                    returnValue = dval.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                } else if (targetType.isEnum()) {
                    // If this is an enum do a case-insensitive comparison
                    Object[] enumConstants = targetType.getEnumConstants();
                    for (Object element : enumConstants) {
                        if (("" + value).equalsIgnoreCase(element.toString())) {
                            returnValue = element;
                            break;
                        }
                    }

                } else if (targetType.equals(UUID.class)) {
                    returnValue = UUID.fromString("" + value);

                } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                    // String to Boolean - T or 1 - otherwise false (or null)
                    String bval = ("" + value).toUpperCase();
                    returnValue = bval.startsWith("T") || bval.startsWith("1");

                } else if (targetType.equals(Time.class)) {
                    // MSSQL works, JTDS returns Varchar in format below with varying decimal numbers
                    // which won't format unless I use Exact so I chop of the milliseconds.
                    DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
                    String sval = "" + value;
                    if (sval.indexOf('.') > -1) {
                        sval = sval.substring(0, sval.indexOf('.'));
                    }
                    dval = tryParseDate(sval, targetType, columnName, timeFormat);
                    returnValue = new Time(dval.getTime());

                } else if (targetType.equals(LocalTime.class)) {
                    // JTDS Fails again...
                    returnValue = LocalTime.parse("" + value);
                } else if (targetType.equals(BigDecimal.class)) {
                    try {
                        returnValue = new BigDecimal("" + value);
                    } catch (NumberFormatException e) {
                        throw new PersismException(Messages.NumberFormatException.message(columnName, targetType, value.getClass(), value), e);
                    }
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

            case UtilDateType: // TODO why utildate here?
            case SQLDateType:
            case TimestampType:
                // TODO if they are the same type we are creating new objects here... Should we?
                if (targetType.equals(java.util.Date.class)) {
                    returnValue = new java.util.Date(((Date) value).getTime());

                } else if (targetType.equals(java.sql.Date.class)) {
                    returnValue = new java.sql.Date(((Date) value).getTime());

                } else if (targetType.equals(Timestamp.class)) {
                    returnValue = new Timestamp(((Date) value).getTime());

                } else if (targetType.equals(LocalDate.class)) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                } else if (targetType.equals(LocalDateTime.class)) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                } else if (targetType.equals(Time.class)) {
                    // Oracle doesn't seem to have Time so we use Timestamp
                    returnValue = new Time(((Date) value).getTime());

                } else if (targetType.equals(LocalTime.class)) {
                    // Oracle.... Sigh
                    Date dt = (Date) value;
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
                }
                break;

            case TimeType:
                if (targetType.equals(LocalTime.class)) {
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
                if (targetType.equals(UUID.class)) {
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
                if (targetType.equals(Blob.class) || targetType.equals(byte[].class) || targetType.equals(Byte[].class)) {
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

    Timestamp tryParseTimestamp(Object value, Class<?> targetType, String columnName) throws PersismException {
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
