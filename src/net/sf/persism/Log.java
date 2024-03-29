package net.sf.persism;


import net.sf.persism.logging.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private AbstractLogger logger;

    private static final Map<String, Log> loggers = new ConcurrentHashMap<String, Log>(12);

    private static final List<String> warnings = new ArrayList<>(32);

    public Log(String logName, LogMode logMode) {
        switch (logMode) {
            case SLF4J -> {
                logger = new Slf4jLogger(logName);
            }
            case LOG4J2 -> {
                logger = new Log4j2Logger(logName);
            }
            case LOG4J -> {
                logger = new Log4jLogger(logName);
            }
            case JUL -> {
                logger = new JulLogger(logName);
            }
        }
    }

    Log(String logName) {
        try {
            Class.forName("org.slf4j.Logger");
            logger = new Slf4jLogger(logName);
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.apache.log4j.Logger");
                logger = new Log4jLogger(logName);
            } catch (ClassNotFoundException e1) {
                try {
                    Class.forName("org.apache.logging.log4j.Logger");
                    logger = new Log4j2Logger(logName);
                } catch (ClassNotFoundException classNotFoundException) {
                    logger = new JulLogger(logName);
                }
            }
        }
    }

    void warnNoDuplicates(String message) {
        String additional = "";

        Throwable throwable = new Throwable("");
        // This finds the stack element for the user's package name - should be the source of the call to include in the message
        Optional<StackTraceElement> opt = Arrays.stream(throwable.getStackTrace()).
                filter(e -> !e.getClassName().startsWith("net.sf.persism")).findFirst();

        if (opt.isPresent()) {
            additional = opt.get().toString().trim();
        }

        String msg = message + " " + additional;
        if (!warnings.contains(msg)) {
            warnings.add(msg);
            warn(msg);
        }
    }

    public static Log getLogger(Class<?> logName) {
        return getLogger(logName.getName());
    }

    // for unit tests
    static Log getLogger(Class<?> logName, LogMode logMode) {
        if (loggers.containsKey(logName.getName())) {
            return loggers.get(logName.getName());
        }
        Log log = new Log(logName.getName(), logMode);
        loggers.put(logName.getName(), log);
        return log;
    }

    public static Log getLogger(String logName) {
        if (loggers.containsKey(logName)) {
            return loggers.get(logName);
        }
        Log log = new Log(logName);
        loggers.put(logName, log);
        return log;
    }

    List<String> warnings() {
        return warnings;
    }


    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(Object message) {
        logger.debug(message);
    }

    public void debug(Object message, Object... params) {
        logger.debug(message, params);
    }

    public void info(Object message) {
        logger.info(message);
    }

    public void info(Object message, Throwable t) {
        logger.info(message, t);
    }

    public void warn(Object message) {
        logger.warn(message);
    }

    public void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    public void error(Object message) {
        logger.error(message);
    }

    public void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    public LogMode getLogMode() {
        return logger.getLogMode();
    }

    public String getLogName() {
        return logger.getLogName();
    }
}
