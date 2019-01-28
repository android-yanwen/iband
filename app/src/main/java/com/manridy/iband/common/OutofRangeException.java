package com.manridy.iband.common;

public class OutofRangeException extends Exception {
	
	private String exceptionMessage;

	private static final long serialVersionUID = 1L;
	
	public OutofRangeException(String message){
		exceptionMessage = message;
	}
	
	public String getMessage(){
		return exceptionMessage;
	}
}
