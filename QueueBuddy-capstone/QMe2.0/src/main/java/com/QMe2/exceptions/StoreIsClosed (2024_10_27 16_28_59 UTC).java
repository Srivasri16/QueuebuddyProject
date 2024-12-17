package com.QMe2.exceptions;

public class StoreIsClosed extends Exception {
	
	private static final long serialVersionUID = 2L;

	public StoreIsClosed(String errorMessage) {
        super(errorMessage);
    }
	
	public StoreIsClosed() {
		super("Store is closed");
	}
}
