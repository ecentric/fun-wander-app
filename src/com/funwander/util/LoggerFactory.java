package com.funwander.util;

import com.funwander.util.Logger.ReportLevel;

/**
 * Tools for creating loggers
 * @author nickolas Shishov
 *
 */
public class LoggerFactory {
	
	/**
	 * Default value
	 */
	private static final ReportLevel defaultLevel = ReportLevel.INFO;
	
	/**
	 * Logger creator
	 * @param clazz
	 * @return
	 */
	public static Logger getLogger(Class clazz) {
		return new Logger(clazz.getName(), defaultLevel);
	}
}
