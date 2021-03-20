package net.sf.persism.logging.implementation;

import net.sf.persism.logging.AbstractLogger;
import net.sf.persism.logging.LogMode;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class JulLogger extends AbstractLogger {

	public JulLogger(String logName) {
		super(LogMode.JUL, logName);
	}

	@Override
	public boolean isDebugEnabled() {
		return Logger.getLogger(logName).isLoggable(Level.FINE);
	}

	@Override
	public void debug(Object message, Object... params) {
		Logger.getLogger(logName).fine(String.format("%s", message));
	}

	@Override
	public void info(Object message) {
		Logger.getLogger(logName).info(String.format("%s", message));
	}

	@Override
	public void info(Object message, Throwable t) {
		Logger.getLogger(logName).log(Level.INFO, String.format("%s", message), t);
	}

	@Override
	public void warn(Object message) {
		Logger.getLogger(logName).warning(String.format("%s", message));
	}

	@Override
	public void warn(Object message, Throwable t) {
		Logger.getLogger(logName).log(Level.WARNING, String.format("%s", message), t);
	}

	@Override
	public void error(Object message) {
		Logger.getLogger(logName).severe(String.format("%s", message));
	}

	@Override
	public void error(Object message, Throwable t) {
		Logger.getLogger(logName).log(Level.SEVERE, String.format("%s", message), t);
	}
}
