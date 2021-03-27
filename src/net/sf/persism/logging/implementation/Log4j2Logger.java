package net.sf.persism.logging.implementation;

import net.sf.persism.logging.AbstractLogger;
import net.sf.persism.logging.LogMode;
import org.apache.logging.log4j.LogManager;

public final class Log4j2Logger extends AbstractLogger {

    public Log4j2Logger(String logName) {
        super(LogMode.LOG4J2, logName);
    }

    @Override
    public boolean isDebugEnabled() {
        return LogManager.getLogger(logName).isDebugEnabled();
    }

    @Override
    public void debug(Object message, Object... params) {
        if (isDebugEnabled()) {
            LogManager.getLogger(logName).debug(String.format("" + message, params));
        }
    }

    @Override
    public void info(Object message) {
        LogManager.getLogger(logName).info(message);
    }

    @Override
    public void info(Object message, Throwable t) {
        LogManager.getLogger(logName).info(message, t);
    }

    @Override
    public void warn(Object message) {
        LogManager.getLogger(logName).warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        LogManager.getLogger(logName).warn(message, t);
    }

    @Override
    public void error(Object message) {
        LogManager.getLogger(logName).error(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        LogManager.getLogger(logName).error(message, t);
    }
}
