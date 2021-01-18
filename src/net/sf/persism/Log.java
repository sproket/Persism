package net.sf.persism;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Logging wrapper to avoid runtime dependencies.
 * It will use slf4j if available or log4j if available otherwise it falls back to JUL (java.util.logging).
 * <p/>
 * JUL will use the default java logging.properties
 * log4j will use log4j.properties
 * slf4j will use logback.xml
 * <p/>
 * log4j DEBUG maps to JUL FINE
 * log4j ERROR maps to JUL SEVERE
 * <p/>
 * The class includes an overloaded error method to log stack traces as well as the message.
 *
 * @author Dan Howard
 * @since 4/21/12 6:47 AM
 */
final class Log {

    private Log() {
    }


    enum LogMode {
        SLF4J, LOG4J, JUL;
    }

    private static LogMode logMode;

    static {
        try {
            Class.forName("org.slf4j.Logger");
            logMode = LogMode.SLF4J;
        } catch (ClassNotFoundException e) {
        }

        if (logMode == null) {
            try {
                Class.forName("org.apache.log4j.Logger");
                logMode = LogMode.LOG4J;
            } catch (ClassNotFoundException e) {
                logMode = LogMode.JUL;
            }
        }

        assert logMode != null;
    }

    private static final Map<String, Log> loggers = new ConcurrentHashMap<String, Log>(12);

    private String logName;

    public Log(String logName) {
        this.logName = logName;
    }

    public static Log getLogger(Class logName) {
        return getLogger(logName.getName());
    }

    public static Log getLogger(String logName) {
        if (loggers.containsKey(logName)) {
            return loggers.get(logName);
        }
        Log log = new Log(logName);
        loggers.put(logName, log);
        return log;
    }


    public boolean isDebugEnabled() {
        switch (logMode) {
            case SLF4J:
                return org.slf4j.LoggerFactory.getLogger(logName).isDebugEnabled();
            case LOG4J:
                return org.apache.log4j.Logger.getLogger(logName).isDebugEnabled();
            default:
                return java.util.logging.Logger.getLogger(logName).isLoggable(Level.FINE);
        }
    }

    public void debug(Object message, Object... params) {
        switch (logMode) {
            case SLF4J:
                org.slf4j.LoggerFactory.getLogger(logName).debug("" + message, params);
                break;
            case LOG4J:
                org.apache.log4j.Logger.getLogger(logName).debug(message);
                break;
            case JUL:
                java.util.logging.Logger.getLogger(logName).fine("" + message);
        }
    }

    public void info(Object message) {
        switch (logMode) {
            case SLF4J:
                org.slf4j.LoggerFactory.getLogger(logName).info("" + message);
                break;
            case LOG4J:
                org.apache.log4j.Logger.getLogger(logName).info(message);
                break;
            case JUL:
                java.util.logging.Logger.getLogger(logName).info("" + message);
        }
    }

    public void warn(Object message) {
        switch (logMode) {
            case SLF4J:
                org.slf4j.LoggerFactory.getLogger(logName).warn("" + message);
                break;
            case LOG4J:
                org.apache.log4j.Logger.getLogger(logName).warn(message);
                break;
            case JUL:
                java.util.logging.Logger.getLogger(logName).warning("" + message);
        }
    }

    public void warn(Object message, Throwable t) {
        switch (logMode) {
            case SLF4J:
                org.slf4j.LoggerFactory.getLogger(logName).warn("" + message, t);
                break;
            case LOG4J:
                org.apache.log4j.Logger.getLogger(logName).warn(message, t);
                break;
            case JUL:
                java.util.logging.Logger.getLogger(logName).log(Level.WARNING, "" + message, t);
        }
    }


    public void error(Object message) {
        switch (logMode) {
            case SLF4J:
                org.slf4j.LoggerFactory.getLogger(logName).error("" + message);
                break;
            case LOG4J:
                org.apache.log4j.Logger.getLogger(logName).error(message);
                break;
            case JUL:
                java.util.logging.Logger.getLogger(logName).severe("" + message);
                break;
        }
    }

    public void error(Object message, Throwable t) {
        switch (logMode) {
            case SLF4J:
                org.slf4j.LoggerFactory.getLogger(logName).error("" + message, t);
                break;
            case LOG4J:
                org.apache.log4j.Logger.getLogger(logName).error(message, t);
                break;
            case JUL:
                java.util.logging.Logger.getLogger(logName).log(Level.SEVERE, "" + message, t);
        }
    }

    public LogMode getLogMode() {
        return logMode;
    }

    public String getLogName() {
        return logName;
    }
}
