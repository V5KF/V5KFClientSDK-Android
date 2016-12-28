package com.v5kf.client.ui.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.v5kf.client.lib.Logger;

import android.annotation.SuppressLint;

public class DateUtil {
	

	public DateUtil() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 获得指定日期的long类型时间TimeInMillis
	 * @param getDate DateUtil 
	 * @return long
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static long getDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month - 1, day);
		
		return calendar.getTimeInMillis();
	}
	
	public static int getYear(long time) {
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		return mCalendar.get(Calendar.YEAR);
	}
	
	public static int getMonth(long time) {
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		return mCalendar.get(Calendar.MONTH) + 1;
	}
	
	public static int getDay(long time) {
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		return mCalendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public static int getYear() {
		Calendar mCalendar = Calendar.getInstance();
		return mCalendar.get(Calendar.YEAR);
	}
	
	public static int getMonth() {
		Calendar mCalendar = Calendar.getInstance();
		return mCalendar.get(Calendar.MONTH) + 1;
	}
	
	public static int getDay() {
		Calendar mCalendar = Calendar.getInstance();
		return mCalendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 当前时间long millisecond
	 * @param getCurrentLongTime DateUtil 
	 * @return long
	 * @return
	 */
	public static long getCurrentLongTime() {
		// TODO Auto-generated method stub
		return (new Date()).getTime();
	}
	
	
	/**
	 * 14位的长整形时间转换
	 * @param timeFormat DateUtil 
	 * @return String
	 * @param time long(milliseconds)
	 * @return
	 */
	public static String timeFormat(long time, boolean isSimple) {
		if (0 == time) {
			time = getCurrentLongTime();
		}		
		return timeFormat(longtimeToDate(time), isSimple);
	}
	
	/**
	 * 字符串格式时间转换
	 * @param timeFormat DateUtil 
	 * @return String
	 * @param time
	 * @return
	 */
	public static String timeFormat(String time, boolean isSimple) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		Date date = null;
		try {
			if (null == time) {
				return time;
			}
			date = format.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		if (null == date) {
			return time;
		}
		
		Calendar target = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		target.setTime(date);	
		if (target.compareTo(now) > 0) {
			target = now;
		}
				
		int yearT = target.get(Calendar.YEAR);
		int hourT = target.get(Calendar.HOUR_OF_DAY);
		int dayOfYear = target.get(Calendar.DAY_OF_YEAR);

		int yearN = now.get(Calendar.YEAR);
		int todayOfYear = now.get(Calendar.DAY_OF_YEAR);
		
		/* 日期 */
		boolean isToday = false;
		String formatTime = "";
		if (yearT < yearN) { // 往年，显示年，月，日即可
			formatTime = "yyyy年MM月dd日";
		} else if (dayOfYear + 1 < todayOfYear) { // 昨天之前
			formatTime = "MM月dd日";
		} else if (dayOfYear + 1 == todayOfYear) { // 昨天
			formatTime = "昨天";
		} else { // 今天
			isToday = true;
		}
		
		
		if (isSimple && !isToday) {
			return (new SimpleDateFormat(formatTime, Locale.SIMPLIFIED_CHINESE)).format(target.getTime());
		}
		
		/* 具体时间(凌晨、早上、中午、下午、晚上) */
		String period = " ";
		if (hourT < 6) { // 凌晨
			period += "凌晨";
		} else if (hourT < 12) { // 早上
			period += "早上";
		} else if (hourT < 14) { // 中午
			period += "中午";
		} else if (hourT < 18) { // 下午
			period += "下午";
		} else { // 晚上
			period += "晚上";
		}
		formatTime += (period + "HH:mm");	
		
		
		return (new SimpleDateFormat(formatTime, Locale.SIMPLIFIED_CHINESE)).format(target.getTime());
	}
	
	/**
	 * milliseconds的long类型时间转指定格式的日期："yyyy-MM-dd HH:mm:ss"
	 * @param longtimeToDate DateUtil 
	 * @return String
	 * @param time milliseconds
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String longtimeToDate(long time) {
		Date now = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");// 可以方便地修改日期格式
		String dateStr = dateFormat.format(now);
		return dateStr;
	}

	/**
	 * milliseconds的long类型时间转指定格式的日期：如"yyyy.MM.dd"或"yyyy-MM-dd HH:mm:ss"
	 * @param longtimeToDayDate DateUtil 
	 * @return String
	 * @param time
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String longtimeToDayDate(long time, String format) {
		Date now = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);// 可以方便地修改日期格式
		String dateStr = dateFormat.format(now);
		return dateStr;
	}

	/**
	 * 获取指定格式的当前日期字符串
	 * @param getCurrentTime DateUtil 
	 * @return String
	 * @param format
	 * @return
	 */
	public static String getCurrentTime(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		String currentTime = sdf.format(date);
		return currentTime;
	}

	/**
	 * 字符串转long格式日期TimeInMillis
	 * @param stringDateToLong DateUtil 
	 * @return long
	 * @param dateStr
	 * @return
	 */
	@SuppressLint("SimpleDateFormat") 
	public static long stringDateToLong(String dateStr) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date.getTime();
	}

	public static String getCurrentTime() {
		return getCurrentTime("yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 指定年月日的格式为"yyyy-MM-dd HH:mm:ss"的字符串
	 * @param getFormatTime DateUtil 
	 * @return String
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static String getFormatTime(int year, int month, int day) {
		long date = getDate(year, month, day);
		return longtimeToDate(date);
	}

	/**
	 * 秒级long日期转化为年月日格式字符串
	 * @param getDayString DateUtil 
	 * @return String
	 * @param mCurrentSearchDay
	 * @return
	 */
	public static String getDayString(long mCurrentSearchDay) {
		// TODO Auto-generated method stub
		long date = mCurrentSearchDay * 1000;
		String day = getYear(date) + "年" + getMonth(date) + "月" + getDay(date) + "日";		
		return day;
	}

	/**
	 * 日期是否是本月
	 * @param isCurrentMonth DateUtil 
	 * @return boolean
	 * @param mCurrentSearchDay
	 * @return
	 */
	public static boolean isOnSameMonth(long day1, long day2) {
		// TODO Auto-generated method stub
		return getMonth(day1 * 1000) == getMonth(day2 * 1000);
	}

	public static int getMonthDays(int year, int month) {  
        if (month > 12) {  
            month = 1;  
            year += 1;  
        } else if (month < 1) {  
            month = 12;  
            year -= 1;  
        }  
        int[] arr = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };  
        int days = 0;  
  
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {  
            arr[1] = 29; // 闰年2月29天  
        }  
  
        try {  
            days = arr[month - 1];  
        } catch (Exception e) {  
            e.getStackTrace();  
        }  
  
        return days;  
    }
	
	public static Calendar getNewMonth(int year, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, 0);
		
		return calendar;
	}

	public static Calendar getNewMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(getYear(), getMonth(), 0);
		
		return calendar;
	}
	
	/**
	 * 格式为"yyyy-MM-dd HH:mm:ss"的字符串取出年月转换成对应long型日期TimeInMillis
	 * (与getDate(int year, int month, int day)结果进行比较)
	 * @param dateStringToLong DateUtil 
	 * @return long
	 * @param date
	 * @return
	 */
	public static long dateStringToLong(String time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		Date date = null;
		try {
			if (null == time) {
				return 0;
			}
			date = format.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,  cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 提取出年月日中的年月信息的日期TimeInMillis
	 * @param getYearAndMonth DateUtil 
	 * @return long milliseconds
	 * @param date milliseconds
	 * @return
	 */
	public static long getYearAndMonth(long date) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTimeInMillis(date);
		Logger.d("[getYearAndMonth]", "Year:" + cal.get(Calendar.YEAR) + " Month:" + (cal.get(Calendar.MONTH) + 1));
		return getDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,  1);
	}

	/**
	 * 系统上线初始时间TimeInMillis
	 * @param getSystemInitTime DateUtil 
	 * @return long
	 * @return
	 */
	public static long getSystemInitTime() {
		// TODO Auto-generated method stub
		return getDate(2015, 8, 31);
	}
}
