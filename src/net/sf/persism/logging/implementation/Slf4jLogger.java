package net.sf.persism.logging.implementation;

import net.sf.persism.logging.AbstractLogger;
import net.sf.persism.logging.LogMode;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
		LoggerFactory.getLogger(logName).debug(String.format("%s", message), params);
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
