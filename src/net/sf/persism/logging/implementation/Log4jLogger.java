package net.sf.persism.logging.implementation;

import net.sf.persism.logging.AbstractLogger;
import net.sf.persism.logging.LogMode;
import org.apache.log4j.Logger;

import java.util.logging.Level;

public final class Log4jLogger extends AbstractLogger {

	public Log4jLogger(String logName) {
		super(LogMode.LOG4J, logName);
	}

	@Override
	public boolean isDebugEnabled() {
		return Logger.getLogger(logName).isDebugEnabled();
	}

	@Override
	public void debug(Object message, Object... params) {
		Logger.getLogger(logName).debug(message);
	}

	@Override
	public void info(Object message) {
		Logger.getLogger(logName).info(message);
	}

	@Override
	public void info(Object message, Throwable t) {
		Logger.getLogger(logName).info(message, t);
	}

	@Override
	public void warn(Object message) {
		Logger.getLogger(logName).warn(message);
	}

	@Override
	public void warn(Object message, Throwable t) {
		Logger.getLogger(logName).warn(message, t);
	}

	@Override
	public void error(Object message) {
		Logger.getLogger(logName).error(message);
	}

	@Override
	public void error(Object message, Throwable t) {
		Logger.getLogger(logName).error(message, t);
	}
}
