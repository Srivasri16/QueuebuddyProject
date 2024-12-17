package com.QMe2.enums;

public enum QueueStatus  {
	Open  (1),
	Closed (2),
	DoesNotExsist (4);
	
	private final int status;
	
	QueueStatus(int i) {
		this.status= i;
	}
}
