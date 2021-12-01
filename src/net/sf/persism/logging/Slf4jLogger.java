package net.sf.persism.logging;

import org.slf4j.LoggerFactory;

/**
 * @hidden
 */
public final class Slf4jLogger extends AbstractLogger {

    public Slf4jLogger(String logName) {
        super(LogMode.SLF4J, logName);
    }

    @Override
    public boolean isDebugEnabled() {
        return LoggerFactory.getLogger(logName).isDebugEnabled();
    }

    @Override
    public void debug(Object message, Object... params) {
        if (isDebugEnabled()) {
            LoggerFactory.getLogger(logName).debug(String.format("" + message, params));
        }
    }

    @Override
    public void debug(Object message) {
        if (isDebugEnabled()) {
            LoggerFactory.getLogger(logName).debug("" + message);
        }
    }

    @Override
    public void info(Object message) {
        LoggerFactory.getLogger(logName).info(String.format("%s", message));
    }

    @Override
    public void info(Object message, Throwable t) {
        LoggerFactory.getLogger(logName).info(String.format("%s", message), t);
    }

    @Override
    public void warn(Object message) {
        LoggerFactory.getLogger(logName).warn(String.format("%s", message));
    }

    @Override
    public void warn(Object message, Throwable t) {
        LoggerFactory.getLogger(logName).warn(String.format("%s", message), t);
    }

    @Override
    public void error(Object message) {
        LoggerFactory.getLogger(logName).error(String.format("%s", message));
    }

    @Override
    public void error(Object message, Throwable t) {
        LoggerFactory.getLogger(logName).error(String.format("%s", message), t);
    }
}
