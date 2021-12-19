package net.sf.persism;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

// https://www.reddit.com/r/java/comments/ma0y6b/java_lambdas_can_deadlock_parallel_class/
// https://stackoverflow.com/questions/45246122/deadlock-caused-by-creating-a-new-thread-during-class-initialization
// Let's not make these static for now....

// HOW TO MAKE THEM have a toString
// https://stackoverflow.com/questions/23628631/how-to-make-a-lambda-expression-define-tostring-in-java-8

final class Conversions {

    private Conversions() {
        
    }
    
    private static final Function<java.util.Date, java.sql.Date> utilDateToSqlDate = date -> new java.sql.Date(date.getTime());

    private static final Function<Date, LocalDate> utilDateToLocalDate = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    private static final Function<java.util.Date, LocalDateTime> utilDateToLocalDateTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    private static final Function<java.util.Date, LocalTime> utilDateToLocalTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

    private static final Function<java.util.Date, java.sql.Timestamp> utilDateToTimestamp = date -> new java.sql.Timestamp(date.getTime());

    private static final Function<java.util.Date, java.sql.Time> utilDateToTime = date -> new java.sql.Time(date.getTime());


    private static final Function<java.sql.Date, java.util.Date> sqlDateToUtilDate = date -> new java.util.Date(date.getTime());

    private static final Function<java.sql.Date, LocalDate> sqlDateToLocalDate = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    private static final Function<java.sql.Date, LocalDateTime> sqlDateToLocalDateTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    private static final Function<java.sql.Date, LocalTime> sqlDateToLocalTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

    private static final Function<java.sql.Date, java.sql.Timestamp> sqlDateToTimestamp = date -> new java.sql.Timestamp(date.getTime());

    private static final Function<java.sql.Date, java.sql.Time> sqlDateToTime = date -> new java.sql.Time(date.getTime());

    private static final Function<java.sql.Timestamp, java.util.Date> timestampToUtilDate = date -> new java.util.Date(date.getTime());

    private static final Function<java.sql.Timestamp, LocalDate> timestampToLocalDate = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    private static final Function<java.sql.Timestamp, LocalDateTime> timestampToLocalDateTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    private static final Function<java.sql.Timestamp, LocalTime> timestampToLocalTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

    private static final Function<java.sql.Timestamp, java.sql.Time> timestampToTime = date -> new java.sql.Time(date.getTime());

    private static final Function<java.sql.Timestamp, java.sql.Date> timestampToSqlDate = date -> new java.sql.Date(date.getTime());

    private static final Function<java.sql.Time, LocalTime> timeToLocalTime = date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

    private static final Function<Integer, Boolean> intToBool = value -> Integer.parseInt("" + value) != 0;

    // SQLite when a Time is defined VIA a convert from LocalTime via Time.valueOf (see getContactForTest)
    private static final Function<Integer, Time> intToTime = value -> new Time(value.longValue());

    // SQLite for Time SQLite sees Long, for LocalTime it sees Integer
    private static final Function<Integer, LocalTime> intToLocalTime = value -> new Time(value).toLocalTime();

    private static final Function<Integer, Short> intToShort = value -> Short.parseShort("" + value);

    private static final Function<Integer, Byte> intToByte = value -> Byte.parseByte("" + value);

    private static final Function<Long, java.sql.Date> longToSqlDate = java.sql.Date::new;

    private static final Function<Long, java.util.Date> longToUtilDate = java.util.Date::new;

    private static final Function<Long, java.sql.Timestamp> longToTimestamp = java.sql.Timestamp::new;

    private static final Function<Long, Integer> longToInt = value -> Integer.parseInt("" + value);

    private static final Function<Long, LocalDate> longToLocalDate = value -> new Timestamp(value).toLocalDateTime().toLocalDate();

    private static final Function<Long, LocalDateTime> longToLocalDateTime = value -> new Timestamp(value).toLocalDateTime();

    private static final Function<Long, Time> longToTime = Time::new;

    private static final Function<Double, BigDecimal> doubleToBigDecimal = value -> new BigDecimal("" + value);

    private static final Function<Double, Float> doubleToFloat = Double::floatValue;

    private static final Function<Double, Integer> doubleToInt = Double::intValue;

    private static final Function<Double, Long> doubleToLong = Double::longValue;

    private static final Function<BigDecimal, Double> bigDecimalToDouble = value -> ((Number) value).doubleValue();

    private static final Function<BigDecimal, Float> bigDecimalToFloat = value -> ((Number) value).floatValue();

    private static final Function<BigDecimal, Long> bigDecimalToLong = value -> ((Number) value).longValue();

    private static final Function<BigDecimal, Integer> bigDecimalToInt = value -> ((Number) value).intValue();

    private static final Function<BigDecimal, Short> bigDecimalToShort = value -> ((Number) value).shortValue();

    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
    private static final Function<BigDecimal, Boolean> bigDecimalToBool = value -> ((Number) value).intValue() == 1;

    private static final Function<BigDecimal, String> bigDecimalToString = value -> "" + value;


    // Read a string but we want a date
    // This condition occurs in SQLite when you have a datetime with default annotated
    // the format returned is 2012-06-02 19:59:49
    // Used for SQLite returning dates as Strings under some conditions
    // SQL or others may return STRING yyyy-MM-dd for older legacy 'date' type.
    // https://docs.microsoft.com/en-us/sql/t-sql/data-types/date-transact-sql?view=sql-server-ver15

    private static final Function<String, java.util.Date> stringToUtilDate = value -> {
        String format;
        if (("" + value).length() > "yyyy-MM-dd".length()) {
            format = "yyyy-MM-dd hh:mm:ss";
        } else {
            format = "yyyy-MM-dd";
        }
        DateFormat df = new SimpleDateFormat(format);
        return Converter.tryParseDate(value, java.util.Date.class, "NO IDEA", df);
    };

    private static final Function<String, java.sql.Date> stringToSqlDate = value -> {
        String format;
        if (("" + value).length() > "yyyy-MM-dd".length()) {
            format = "yyyy-MM-dd hh:mm:ss";
        } else {
            format = "yyyy-MM-dd";
        }
        DateFormat df = new SimpleDateFormat(format);
        return new java.sql.Date(Converter.tryParseDate(value, java.util.Date.class, "NO IDEA", df).getTime());
    };

    // TODO handle better message
    private static final Function<String, java.sql.Timestamp> stringToTimestamp = value -> Timestamp.valueOf("" + value);

    // string to localdate string to localdatetime = JTDS - fuck off

    // Not sure about this one...
    // If this is an enum do a case-insensitive comparison
    private static final BiFunction<String, Class<?>, Enum<?>> stringToEnum = (value, enumCLass) -> {
        assert enumCLass.isEnum();

        Object[] enumConstants = enumCLass.getEnumConstants();
        for (Object element : enumConstants) {
            if (("" + value).equalsIgnoreCase(element.toString())) {
                return (Enum<?>) element;
            }
        }
        return null;
    };

    private static final Function<byte[], UUID> byteArrayToUUID = Converter::asUuid;

    private static final Function<String, UUID> stringToUUID = UUID::fromString;

    private static final Function<UUID, byte[]> UUIDtoByteArray = Converter::asBytes;

    private static final Function<String, Boolean> stringToBoolean = value -> {
        // String to Boolean - T or 1 - otherwise false (or null)
        String bval = ("" + value).toUpperCase();
        return bval.startsWith("T") || bval.startsWith("1");
    };

    // todo runtime exceptions can be checked by property/columninfo calls to add the extra meta info

    // todo this throw new PersismException(Messages.NumberFormatException.message(columnName, columnType, value.getClass(), value), e);
    private static final Function<String, BigDecimal> stringToBigDecimal = value -> new BigDecimal("" + value);

    private static final Function<LocalDate, java.sql.Date> localDateToSqlDate = java.sql.Date::valueOf;

    private static final Function<LocalDateTime, java.sql.Timestamp> localDateToTimestamp = java.sql.Timestamp::valueOf;

    private static final Function<LocalTime, java.sql.Time> localTimeToTime = java.sql.Time::valueOf;

     public static Function<Date, java.sql.Date> UtilDateToSqlDate() {
        return utilDateToSqlDate;
    }

     public static Function<Date, LocalDate> UtilDateToLocalDate() {
        return utilDateToLocalDate;
    }

     public static Function<Date, LocalDateTime> UtilDateToLocalDateTime() {
        return utilDateToLocalDateTime;
    }

     public static Function<Date, LocalTime> UtilDateToLocalTime() {
        return utilDateToLocalTime;
    }

     public static Function<Date, Timestamp> UtilDateToTimestamp() {
        return utilDateToTimestamp;
    }

     public static Function<Date, Time> UtilDateToTime() {
        return utilDateToTime;
    }

     public static Function<java.sql.Date, Date> SqlDateToUtilDate() {
        return sqlDateToUtilDate;
    }

     public static Function<java.sql.Date, LocalDate> SqlDateToLocalDate() {
        return sqlDateToLocalDate;
    }

     public static Function<java.sql.Date, LocalDateTime> SqlDateToLocalDateTime() {
        return sqlDateToLocalDateTime;
    }

     public static Function<java.sql.Date, LocalTime> SqlDateToLocalTime() {
        return sqlDateToLocalTime;
    }

     public static Function<java.sql.Date, Timestamp> SqlDateToTimestamp() {
        return sqlDateToTimestamp;
    }

     public static Function<java.sql.Date, Time> SqlDateToTime() {
        return sqlDateToTime;
    }

     public static Function<Timestamp, Date> TimestampToUtilDate() {
        return timestampToUtilDate;
    }

     public static Function<Timestamp, LocalDate> TimestampToLocalDate() {
        return timestampToLocalDate;
    }

     public static Function<Timestamp, LocalDateTime> TimestampToLocalDateTime() {
        return timestampToLocalDateTime;
    }

     public static Function<Timestamp, LocalTime> TimestampToLocalTime() {
        return timestampToLocalTime;
    }

     public static Function<Timestamp, Time> TimestampToTime() {
        return timestampToTime;
    }

     public static Function<Timestamp, java.sql.Date> TimestampToSqlDate() {
        return timestampToSqlDate;
    }

     public static Function<Time, LocalTime> TimeToLocalTime() {
        return timeToLocalTime;
    }

    public static Function<Integer, Boolean> IntToBoolean() {
        return intToBool;
    }

    public static Function<Integer, Time> IntToTime() {
        return intToTime;
    }

    public static Function<Integer, LocalTime> IntToLocalTime() {
        return intToLocalTime;
    }

     public static Function<Integer, Short> IntToShort() {
        return intToShort;
    }

     public static Function<Integer, Byte> IntToByte() {
        return intToByte;
    }

     public static Function<Long, java.sql.Date> LongToSqlDate() {
        return longToSqlDate;
    }

     public static Function<Long, Date> LongToUtilDate() {
        return longToUtilDate;
    }

     public static Function<Long, Timestamp> LongToTimestamp() {
        return longToTimestamp;
    }

     public static Function<Long, Integer> LongToInt() {
        return longToInt;
    }

     public static Function<Long, LocalDate> LongToLocalDate() {
        return longToLocalDate;
    }

     public static Function<Long, LocalDateTime> LongToLocalDateTime() {
        return longToLocalDateTime;
    }

     public static Function<Long, Time> LongToTime() {
        return longToTime;
    }

     public static Function<Double, BigDecimal> DoubleToBigDecimal() {
        return doubleToBigDecimal;
    }

     public static Function<Double, Float> DoubleToFloat() {
        return doubleToFloat;
    }

     public static Function<Double, Integer> DoubleToInt() {
        return doubleToInt;
    }

     public static Function<Double, Long> DoubleToLong() {
        return doubleToLong;
    }

     public static Function<BigDecimal, Double> BigDecimalToDouble() {
        return bigDecimalToDouble;
    }

     public static Function<BigDecimal, Float> BigDecimalToFloat() {
        return bigDecimalToFloat;
    }

     public static Function<BigDecimal, Long> BigDecimalToLong() {
        return bigDecimalToLong;
    }

     public static Function<BigDecimal, Integer> BigDecimalToInt() {
        return bigDecimalToInt;
    }

     public static Function<BigDecimal, Short> BigDecimalToShort() {
        return bigDecimalToShort;
    }

     public static Function<BigDecimal, Boolean> BigDecimalToBool() {
        return bigDecimalToBool;
    }

     public static Function<BigDecimal, String> BigDecimalToString() {
        return bigDecimalToString;
    }

     public static Function<String, Date> StringToUtilDate() {
        return stringToUtilDate;
    }

     public static Function<String, java.sql.Date> StringToSqlDate() {
        return stringToSqlDate;
    }

     public static Function<String, Timestamp> StringToTimestamp() {
        return stringToTimestamp;
    }

    public static BiFunction<String, Class<?>, Enum<?>> StringToEnum() {
        return stringToEnum;
    }

     public static Function<byte[], UUID> ByteArrayToUUID() {
        return byteArrayToUUID;
    }

     public static Function<String, UUID> StringToUUID() {
        return stringToUUID;
    }

     public static Function<UUID, byte[]> UUIDtoByteArray() {
        return UUIDtoByteArray;
    }

     public static Function<String, Boolean> StringToBoolean() {
        return stringToBoolean;
    }

     public static Function<String, BigDecimal> StringToBigDecimal() {
        return stringToBigDecimal;
    }

     public static Function<LocalDate, java.sql.Date> LocalDateToSqlDate() {
        return localDateToSqlDate;
    }

     public static Function<LocalDateTime, Timestamp> LocalDateToTimestamp() {
        return localDateToTimestamp;
    }

     public static Function<LocalTime, Time> LocalTimeToTime() {
        return localTimeToTime;
    }
}
