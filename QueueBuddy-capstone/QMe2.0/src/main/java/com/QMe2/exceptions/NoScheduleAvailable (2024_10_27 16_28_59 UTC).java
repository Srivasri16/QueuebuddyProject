package com.QMe2.exceptions;

public class NoScheduleAvailable extends Exception {
	

	private static final long serialVersionUID = 1L;

	public NoScheduleAvailable(String errorMessage) {
        super(errorMessage);
    }
	
	public NoScheduleAvailable() {
		super("No schedule available");
	}
}
