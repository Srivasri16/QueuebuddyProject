package com.QMe2.enums;

public enum QueuerStatus {
	INLINE  (1),
	CALLED (2),
	CANCELED (4);
	
	private final int status;
	
	QueuerStatus(int i) {
		this.status= i;
	}
}
