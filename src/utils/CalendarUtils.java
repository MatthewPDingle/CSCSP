package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CalendarUtils {
	
	public static long difference(Calendar c1, Calendar c2, int unit) { 
		differenceCheckUnit(unit); 
		Map<Integer, Long> unitEstimates = differenceGetUnitEstimates(); 
		Calendar first = (Calendar) c1.clone(); 
		Calendar last = (Calendar) c2.clone(); 
		long difference = c2.getTimeInMillis() - c1.getTimeInMillis(); 
		long unitEstimate = unitEstimates.get(unit).longValue(); 
		long increment = (long) Math.floor((double) difference / (double) unitEstimate); 
		increment = Math.max(increment, 1); long total = 0; 
		while (increment > 0) { 
			add(first, unit, increment); 
			if (first.after(last)) { 
				add(first, unit, increment * -1); 
				increment = (long) Math.floor(increment / 2); 
			} 
			else { 
				total += increment; 
			} 
		} 
		return total; 
	} 
	
	private static Map<Integer, Long> differenceGetUnitEstimates() { 
		Map<Integer, Long> unitEstimates = new HashMap<Integer, Long>(); 
		unitEstimates.put(Calendar.YEAR, 1000l * 60 * 60 * 24 * 365); 
		unitEstimates.put(Calendar.MONTH, 1000l * 60 * 60 * 24 * 30); 
		unitEstimates.put(Calendar.DAY_OF_MONTH, 1000l * 60 * 60 * 24); 
		unitEstimates.put(Calendar.HOUR_OF_DAY, 1000l * 60 * 60); 
		unitEstimates.put(Calendar.MINUTE, 1000l * 60); 
		unitEstimates.put(Calendar.SECOND, 1000l); 
		unitEstimates.put(Calendar.MILLISECOND, 1l); 
		return unitEstimates; 
	} 

	private static void differenceCheckUnit(int unit) { 
		List<Integer> validUnits = new ArrayList<Integer>(); 
		validUnits.add(Calendar.YEAR); 
		validUnits.add(Calendar.MONTH); 
		validUnits.add(Calendar.DAY_OF_MONTH); 
		validUnits.add(Calendar.HOUR_OF_DAY); 
		validUnits.add(Calendar.MINUTE); 
		validUnits.add(Calendar.SECOND); 
		validUnits.add(Calendar.MILLISECOND); 
		if (!validUnits.contains(unit)) { 
			throw new RuntimeException( "CalendarUtils.difference one of these units Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND." ); 
		} 
	} 

	public static void add(Calendar c, int unit, long increment) { 
		while (increment > Integer.MAX_VALUE) { 
			c.add(unit, Integer.MAX_VALUE); 
			increment -= Integer.MAX_VALUE; 
		} 
		c.add(unit, (int) increment); 
	} 
	
	/**
	 * Converts a SQL Date object into yyyy-MM-dd format
	 * (usually so it can be inserted into a DB table.)
	 * 
	 * @param c
	 * @return
	 */
	public static String getSqlDateString(Calendar c) {
		try {
			String year = new Integer(c.get(Calendar.YEAR)).toString();
			String month = new Integer(c.get(Calendar.MONTH) + 1).toString();
			String day = new Integer(c.get(Calendar.DATE)).toString();
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1) 
				day = "0" + day;
			String date = year + "-" + month + "-" + day;
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Converts a SQL Date object into yyyy-MM-dd hh:mm:ss format
	 * (usually so it can be inserted into a DB table.)
	 * 
	 * @param c
	 * @return
	 */
	public static String getSqlDateTimeString(Calendar c) {
		try {
			String year = new Integer(c.get(Calendar.YEAR)).toString();
			String month = new Integer(c.get(Calendar.MONTH) + 1).toString();
			String day = new Integer(c.get(Calendar.DATE)).toString();
			String hour = new Integer(c.get(Calendar.HOUR_OF_DAY)).toString();
			String minute = new Integer(c.get(Calendar.MINUTE)).toString();
			String second = new Integer(c.get(Calendar.SECOND)).toString();
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1) 
				day = "0" + day;
			if (hour.length() == 1) 
				hour = "0" + hour;
			if (minute.length() == 1)
				minute = "0" + minute;
			if (second.length() == 1)
				second = "0" + second;
			String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Returns MM-dd-yyyy
	 * 
	 * @param c
	 * @return
	 */
	public static String getGUIDateString(Calendar c) {
		try {
			String year = new Integer(c.get(Calendar.YEAR)).toString();
			String month = new Integer(c.get(Calendar.MONTH) + 1).toString();
			String day = new Integer(c.get(Calendar.DATE)).toString();
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1) 
				day = "0" + day;
			String date = month + "-" + day + "-" + year;
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}