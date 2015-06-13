package com.funwander.util;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Handle exceptions. Aim of this handler is catching unchecked exceptions for
 * sending report to developers
 * 
 * @author nickolas
 * 
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

	/**
	 * Default exception handler
	 */
	private UncaughtExceptionHandler sysHandler;
	private static Logger logger = LoggerFactory
			.getLogger(ExceptionHandler.class);

	/**
	 * 
	 * @param sysHandler
	 *            - default exception handler
	 */
	public ExceptionHandler(UncaughtExceptionHandler sysHandler) {
		this.sysHandler = sysHandler;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		logger.exception(ex);
		sysHandler.uncaughtException(thread, ex);
	}

}
