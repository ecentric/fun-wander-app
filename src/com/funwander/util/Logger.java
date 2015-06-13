package com.funwander.util;

/**
 * 
 * TODO: add support reporting to Internet service
 * @author nickolas
 *
 */
public class Logger {

	private String name;
	private ReportLevel level;

	/**
	 * Log levels
	 */
	public enum ReportLevel {
		EXCEPTION, ERROR, WARNING, DEBUG, INFO
	}

	public void setLevel(String level) {
		this.level = ReportLevel.valueOf(level);
	}

	public Logger(String name, ReportLevel level) {
		this.name = name;
		this.level = level;
	}

	public void error(String message) {
		report(ReportLevel.ERROR, message);
	}

	public void warning(String message) {
		report(ReportLevel.WARNING, message);
	}

	public void info(String message) {
		report(ReportLevel.INFO, message);
	}

	public void debug(String message) {
		report(ReportLevel.DEBUG, message);
	}

	public void exception(Throwable e) {
		StringBuilder trace = new StringBuilder();
		trace.append(e.getClass() + " ");
		trace.append(e.getMessage());
		trace.append(System.getProperty("line.separator"));
		for (StackTraceElement el : e.getStackTrace()) {
			trace.append(String.format(" at %s(%s:%d)", el.getClassName(),
					el.getFileName(), el.getLineNumber()));
			trace.append(System.getProperty("line.separator"));
		}
		report(ReportLevel.EXCEPTION, trace.toString());
		Throwable cause = e.getCause(); 
		if (cause != null) exception(cause);
	}

	/**
	 * Make report
	 * 
	 * @param level
	 * @param message
	 */
	private void report(ReportLevel level, String message) {
		if (level.ordinal() > this.level.ordinal())
			return;

		System.out.println(String.format("%s %s : %s", level.toString(), name,
				message));
	}
}
