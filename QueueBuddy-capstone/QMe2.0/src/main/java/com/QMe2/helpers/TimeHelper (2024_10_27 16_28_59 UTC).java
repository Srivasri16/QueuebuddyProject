package com.QMe2.helpers;

import java.util.Calendar;

public class TimeHelper {
	public static Calendar nowAt0() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 12);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now;
	}
}
