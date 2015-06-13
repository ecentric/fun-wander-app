package com.funwander.util;

/**
 * Auxiliary exception
 * @author nickolas
 *
 */
@SuppressWarnings("serial")
public class AppException extends Exception {
	
	private Exception subException = null;

	public AppException(String message) {
		super(message);
	}
	
	public AppException() {
		super();
	}
	
	public AppException(Exception e) {
		super();
		subException = e;
	}
	
	public StackTraceElement[] getStackTrace() {
		if (subException != null) {
			return subException.getStackTrace();			
		} 
		return super.getStackTrace();
	}
	
}
