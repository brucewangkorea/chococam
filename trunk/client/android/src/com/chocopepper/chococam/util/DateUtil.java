package com.chocopepper.chococam.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;

public class DateUtil {

//	private static final String TAG = Logger.makeLogTag(DateUtil.class);

	private static final long SECOND 	= (long)1000;
	private static final long MINUTE 	= SECOND * 60;
	private static final long HOUR 		= MINUTE * 60;
	private static final long DAY 		= HOUR * 24;
	private static final long MONTH 	= DAY * 30;
	
	/*
	 * 2012-10-08 brucewang
	 * Rails가 전달해 주는 시간정보는 UTC 시간으로, 이것을 현재 디바이스의 시각과 비교하기 위해선
	 * 현재 디바이스의 time offset을 더해주어야 함.
	 * 다음의 링크 참고.
	 * http://susemi99.tistory.com/804
	 */
	public static Date getDateTimeAddLocalTimezone(String date)
	{
	    Date d = new Date();
	    try {
	        d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date);
	        Time t = new Time();
	        Long l = t.normalize(t.isDst==0);
	        Long between = l/1000/60/60;
	        d.setHours(d.getHours() - Long.valueOf(between).intValue());
	        return d;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return d;
	}
	
	public static String getDifferentTime(String strdate) {
		return getDifferentTime(getDateTimeAddLocalTimezone(strdate).getTime());
	}
	
	/**
	 * 시간 차이를 알아내는 함수
	 * @param targetTimeMillis
	 * @return
	 */
	private static String getDifferentTime(long targetTimeMillis) {
		long currTime = new Date().getTime();//System.currentTimeMillis();
		long diffTime = currTime - targetTimeMillis;

		String returnValue = "";
		if (diffTime < MINUTE) {
			returnValue = "방금 전";
		} else if (diffTime < HOUR) {
			returnValue = (diffTime / MINUTE) + "분 전";
		} else if (diffTime < DAY) {
			returnValue = (diffTime / HOUR) + "시간 전";
		} else if (diffTime < MONTH) {			
			returnValue = (diffTime / DAY) + "일 전";
		} else {
			returnValue = (diffTime / MONTH) + "달 전";
		}
		
		if(returnValue.equals("1일 전"))
		{
			returnValue = "어제";
		}
		
		return returnValue;
	}
	public static boolean isCurrentDate(String date) {
		return getCurrentDate().equals(date);
	}

	public static String getCurrentDate() {
		String dateTime;
		Calendar c = Calendar.getInstance();
		
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH)+1;
		int curDay = c.get(Calendar.DAY_OF_MONTH);
		dateTime = curYear + "-" + curMonth + "-" + curDay;
		
		return dateTime;
	}
	
	public static String getCurrentTimeWithoutNoon() {
		StringBuilder dateTime = new StringBuilder();
		Calendar c = Calendar.getInstance();
		
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH)+1;
		int curDay = c.get(Calendar.DAY_OF_MONTH);  
		// 20120523_arisu717 - 피드 > 댓글: 댓글 등록 직후 표시 시간 이상 [[
		//int curHour = c.get(Calendar.HOUR);
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		// ]]
		int curMinute = c.get(Calendar.MINUTE);
		int curSecond = c.get(Calendar.SECOND);
		// 20120523_arisu717 - 피드 > 댓글: 댓글 등록 직후 표시 시간 이상 [[
		//dateTime.append(curYear).append("-").append(curMonth).append("-").append(curDay).append(" ");
		//dateTime.append(curHour).append(":").append(curMinute).append(":").append(curSecond);
		dateTime.append(
				String.format("%02d-%02d-%02d %02d:%02d:%02d",
						curYear, curMonth, curDay, curHour, curMinute, curSecond));
		// ]]
		return dateTime.toString();
	}
	
	public static String getCurrentTime() {
		StringBuilder dateTime = new StringBuilder();
		Calendar c = Calendar.getInstance();
		
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH)+1;
		int curDay = c.get(Calendar.DAY_OF_MONTH);  
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curNoon = c.get(Calendar.AM_PM);
		String noon;
		if (curNoon == 0) {
			noon = "AM";
		} else {
			noon = "PM";
			curHour -= 12;
		}
		int curMinute = c.get(Calendar.MINUTE);
		int curSecond = c.get(Calendar.SECOND);
		dateTime.append(curYear).append("-").append(curMonth).append("-").append(curDay).append(" ");
		dateTime.append(curHour).append(":").append(curMinute).append(":").append(curSecond).append(noon);
		return dateTime.toString();
	}
	
	public static String[] getCurrentDateTime() {
		String[] dateTime = new String[2];
		Calendar c = Calendar.getInstance();
		
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH)+1;
		int curDay = c.get(Calendar.DAY_OF_MONTH);  
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		// 20120523_arisu717 - 피드 > 댓글: 댓글 등록 직후 표시 시간 이상 [[
		//int curNoon = c.get(Calendar.AM_PM);
		//String noon;
		//if (curNoon == 0) {
		//	noon = "AM";
		//} else {
		//	noon = "PM";
		//	curHour -= 12;
		//}
		// ]]
		int curMinute = c.get(Calendar.MINUTE);   
		int curSecond = c.get(Calendar.SECOND);
		// 20120523_arisu717 - 피드 > 댓글: 댓글 등록 직후 표시 시간 이상 [[
		//dateTime[0] = curYear + "-" + curMonth + "-" + curDay;
		//dateTime[1] = curHour + ":" + curMinute + ":" + curSecond + noon;
		dateTime[0] = String.format("%02d-%02d-%02d", curYear, curMonth, curDay);
		dateTime[1] = String.format("%02d:%02d:%02d", curHour, curMinute, curSecond);
		// ]]
		
		return dateTime;
	}
}
