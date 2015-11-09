package com.reserve.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.reserve.constants.TicketConstants;

public class DateUtils {

	// 5 mins before date
	public static Date getOutdatedDate() {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -(TicketConstants.HOLD_MINUTES));
		return calendar.getTime();
	}

	public static String getValidUntilDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		String reportDate = null;
		DateFormat df = null;
		try {
			df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			calendar.setTime(date);
			calendar.add(Calendar.MINUTE, (TicketConstants.HOLD_MINUTES));
			reportDate = df.format(calendar.getTime());
		} catch (Exception exception) {
			// Do nothing
		}
		return reportDate;
	}
}
