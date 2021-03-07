package net.sf.persism.logging;

public abstract class AbstractLogger {

	protected final LogMode logMode;
	protected final String logName;

	public AbstractLogger(LogMode logMode, String logName) {
		this.logMode = logMode;
		this.logName = logName;
	}

	public abstract boolean isDebugEnabled();

	public abstract void debug(Object message, Object... params);

	public abstract void info(Object message);

	public abstract void warn(Object message);

	public abstract void warn(Object message, Throwable t);

	public abstract void error(Object message);

	public abstract void error(Object message, Throwable t);

	public LogMode getLogMode() {
		return logMode;
	}

	public String getLogName() {
		return logName;
	}
}
