/**
 * 
 */
package com.yeyeck.vertx.util;

import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DateUtils {
	public static final String START_TIME = "startTime";
    public final static String HMS = "HH:mm:ss";	
	public static String FMT_YMD = "yyyyMMdd";
	public static String FMT_YYMD = "yyyy-MM-dd";
	public static String FMT_YYMDHMS = "yyyy-MM-dd HH:mm:ss";
	public static String FMT_YYMDHMSS = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String DAY_START = "00:00:00";

	public static final String DAY_END="23:59:59";

	public final static String[] weeks = {"周日","周一","周二","周三","周四","周五","周六"};

	//一天的秒数
	public static final long SECONDS_IN_DAY = 24 * 60 * 60 * 1000L;

	//全年天数
	public static final long DAYS_IN_YEAR=365;
	
	public static String ms() {
		return getTime(FMT_YYMDHMSS);
	}

	public static String getTime(String pattern) {
		LocalDateTime ldt = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
		String dateStr = ldt.format(dateTimeFormatter);
		return dateStr;
	}
	
	/**
	 * 取出最小的日期 <br/>
	 * @author jpq
	 * @param dates 仅支持的类型：LocalDate，yyyy-MM-dd格式的字符串
	 * @return
	 */
	public static LocalDate min(Object... dates) {
		if(ArrayUtils.isEmpty(dates)) {
			return null;
		}
		LocalDate ret = null;
		LocalDate tmp = null;
		for(int i=0; i<dates.length; i++) {
			tmp = toLocalDate(dates[i]);
			if(null == tmp) {
				continue;
			}
			if(null == ret) {
				ret = tmp;
				continue;
			}
			if(ret.isAfter(tmp)) {
				ret = tmp;
			}
		}
		return ret;
	}
	
	public static LocalDate toLocalDate(Object date) {
		if(null == date || StringUtils.isBlank(date.toString())) {
			return null;
		}
		return date instanceof LocalDate ? (LocalDate) date : LocalDate.parse(date.toString());
	}

	/**
	 * 日期加减天数
	 * @param date	日期
	 * @param day	天数
	 */
	public static Date addOrSubtractDay(Date date, int day){
		Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(date);
		fromCal.add(Calendar.DATE, day);
		return fromCal.getTime();
	}
	
	public static long diffMs(String start, String end, String pattern) {
		return ChronoUnit.MILLIS.between(parseDateTime(start, pattern), parseDateTime(end, pattern));
	}
	
	public static long diffMs(Temporal start, Temporal end) {
		return ChronoUnit.MILLIS.between(start, end);
	}

	/**
	 * 日期间隔天数 <br/>
	 * @author jiangpq
	 * @param start
	 * @param end
	 * @return
	 */
	public static long diffDay(String start, String end) {
		return ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.parse(end));
	}
	public static long diffDay(Temporal start, Temporal end) {
		return ChronoUnit.DAYS.between(start, end);
	}
	public static int diffMon(Temporal start, Temporal end) {
		return (int) ChronoUnit.MONTHS.between(start, end);
	}
	public static int diffYear(Temporal start, Temporal end) {
		return (int) ChronoUnit.YEARS.between(start, end);
	}
	/**
	 * 获取范围内的年初值 <br/>
	 * @author jiangpq
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<LocalDate> splitYears(String start, String end) {
		return splitYears(LocalDate.parse(start), LocalDate.parse(end));
	}
	public static List<LocalDate> splitYears(LocalDate start, LocalDate end) {
		List<LocalDate> ret = new ArrayList<>();
		while(!end.isBefore(start)) {
			if(start.getDayOfYear() == 1) {
				ret.add(start);
			} else {
				start = start.withDayOfYear(1);
			}
			start = start.plusYears(1);
		}
		return ret;
	}
	
	/**
	 * 按年分割时间段 <br/>
	 * @author jiangpq
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<Tuple2<LocalDate, LocalDate>> splitYear(String start, String end) {
		return splitYear(LocalDate.parse(start), LocalDate.parse(end));
	}
	public static List<Tuple2<LocalDate, LocalDate>> splitYear(LocalDate start, LocalDate end) {
		List<Tuple2<LocalDate, LocalDate>> ret = new ArrayList<>();
		LocalDate endTemp = start;
		while(!end.isBefore(start)) {
			endTemp = start.plusYears(1).withDayOfYear(1).minusDays(1);
			endTemp = endTemp.isAfter(end) ? end : endTemp;
			ret.add(new Tuple2<>(start, endTemp));
			if(endTemp.isEqual(end)) {
				break;
			}
			start = endTemp.plusDays(1);
		}
		return ret;
	}

	public static List<Tuple2<LocalDate, LocalDate>> splitMons(String start, String end) {
		return splitMons(LocalDate.parse(start), LocalDate.parse(end));
	}
	public static List<Tuple2<LocalDate, LocalDate>> splitMons(LocalDate start, LocalDate end) {
		List<Tuple2<LocalDate, LocalDate>> ret = new ArrayList<>();
		LocalDate endTemp = start;
		while(!end.isBefore(start)) {
			endTemp = start.plusMonths(1).withDayOfMonth(1).minusDays(1);
			endTemp = endTemp.isAfter(end) ? end : endTemp;
			ret.add(new Tuple2<>(start, endTemp));
			if(endTemp.isEqual(end)) {
				break;
			}
			start = endTemp.plusDays(1);
		}
		return ret;
	}

	/**
	 * @Des 按周分割时间段
	 * @Author: Mr.Xie
	 * @Date: 2024.04.22 11:15
	 */
	public static List<Tuple2<LocalDate, LocalDate>> splitWeek(String start, String end) {
		return splitWeek(LocalDate.parse(start), LocalDate.parse(end));
	}
	public static List<Tuple2<LocalDate, LocalDate>> splitWeek(LocalDate start, LocalDate end) {
		List<Tuple2<LocalDate, LocalDate>> ret = new ArrayList<>();
		LocalDate monday = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		if(!start.isEqual(monday)){
			ret.add(new Tuple2<>(start, monday.minusDays(1)));
		}
		while(!monday.isAfter(end)) {
			LocalDate sunday = monday.plusDays(6);
			sunday = sunday.isAfter(end) ? end : sunday;
			ret.add(new Tuple2<>(monday, sunday));
			monday = sunday.plusDays(1);
		}
		return ret;
	}

	/**
	 * 返回大于等于开始日期并且小于等于结束日期的所有日期集合
	 *
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static List<String> collect(String beginDate, String endDate) {
		List<String> dates = new ArrayList<>();
		LocalDate beg = LocalDate.parse(beginDate);
		LocalDate end = LocalDate.parse(endDate);
		while (!beg.isAfter(end)){
			dates.add(beg.toString());
			beg = beg.plusDays(1);
		}
		return dates;
	}


	public static List<String> collectYear(String beginDate, String endDate) {
		return collectYear(LocalDate.parse(beginDate), LocalDate.parse(endDate));
	}
	
	/**
	 * 计算年份集合
	 *
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static List<String> collectYear(LocalDate beginDate, LocalDate endDate) {
		int start = beginDate.getYear();
		int end = endDate.getYear();
		List<String> dates = new ArrayList<>();
		while (start <= end){
			dates.add(String.valueOf(start));
			start++;
		}
		return dates.stream().distinct().collect(Collectors.toList());
	}

	public static boolean isLocalDate(String str){
		boolean flag = true;
		try {
			LocalDate.parse(str.trim());
		}catch (Exception e){
			flag = false;
		}
		return flag;
	}

	/**
	 * 数据断层型，没有从年初开始！ <br/>
	 * @author jiangpq
	 * @param lastDate
	 * @param date
	 * @return
	 */
	public static boolean isYearStart(String lastDate, String date){
		return isYearStart(date) || !date.substring(0, 4).equals(StringUtils.substring(lastDate, 0, 4));
	}
	public static boolean isYearStart(String str){
		return StringUtils.isNotBlank(str) && isYearStart(toLocalDate(str));
	}
	public static boolean isYearStart(LocalDate date){
		return null != date && date.getDayOfYear() == 1;
	}
	public static LocalDate addDays(String str, int days) {
		LocalDate date = toLocalDate(str);
		return date.plusDays(days);
	}

	/**
	 * @Des 返回季度第一天
	 * @Author: Mr.Xie
	 * @Date: 2023.04.24 15:46
	 */
	public static LocalDate firstDayOfQuarter(LocalDate date){
		Month firstMonthOfQuarter = date.getMonth().firstMonthOfQuarter();
		return LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
	}

	/**
	 * @Des 返回季度最后一天
	 * @Author: Mr.Xie
	 * @Date: 2023.04.24 15:46
	 */
	public static String lastDayOfQuarter(LocalDate date){
		Month endMonthOfQuarter = Month.of(date.getMonth().firstMonthOfQuarter().getValue() + 2);
		int monthLength = endMonthOfQuarter.length(date.isLeapYear());
		return LocalDate.of(date.getYear(), endMonthOfQuarter, monthLength).toString();
	}
	
    public static String format(TemporalAccessor temporal, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(temporal);
    }
    public static String format(TemporalAccessor temporal) {
    	String pattern = temporal instanceof LocalTime ? HMS : (temporal instanceof LocalDate ? FMT_YYMD : FMT_YYMDHMS);
        return DateTimeFormatter.ofPattern(pattern).format(temporal);
    }

	public static LocalDateTime parseDateTime(String time) {
		return parseDateTime(time, FMT_YYMDHMS);
	}
	public static LocalDateTime parseDateTime(String time, String format) {
		return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(format));
	}
	public static LocalDate parseDate(String time) {
		return LocalDate.parse(time, DateTimeFormatter.ofPattern(FMT_YMD));
	}
	public static LocalDate parseDate(String time, String format) {
		return LocalDate.parse(time, DateTimeFormatter.ofPattern(format));
	}
	public static String parseStr(LocalDate localDate, String pattern) {
		return localDate.format(DateTimeFormatter.ofPattern(pattern));
	}
	public static String parseStr(String time, String pattern) {
		return Try.of(() -> parseStr(parseDate(time), pattern)).onFailure(ex -> log.error("日期={}，转换异常：", time, ex)).get();
	}
	public static LocalDateTime cvtLocalDate(String localDate, String pattern) {
		LocalDate date = parseDate(localDate, pattern);
		return LocalDateTime.of(date, LocalTime.MIN);
	}

	public static LocalDateTime parseDateTime(String date, int hour, int minute, int second){
		return LocalDateTime.of(LocalDate.parse(date), LocalTime.of(hour, minute, second));
	}

	public static LocalDate toLocalDate(String date, String pattern){
		return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
	}
	
	public static long ms(LocalDateTime ldt) {
		return ldt.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
	}

    /**
     * 解析时间 <br/>
     * @author jiangpeiquan
     * @param dateStr
     * @return
     */
	public static LocalDate parseAnyDate(String dateStr) {
		LocalDateTime anyDateTime = parseAnyDateTime(dateStr);
		if(null == anyDateTime) {
			return null;
		}
		return anyDateTime.toLocalDate();
	}

    /**
     * 解析时间 <br/>
     * @author jiangpeiquan
     * @param dateStr
     * @return
     */
	public static LocalDateTime parseAnyDateTime(String dateStr) {
		if(StringUtils.isBlank(dateStr)) {
			return null;
		}
		try {  // 20240611102615.342000+480
			dateStr = (dateStr.contains("+") ? dateStr.split("\\+")[0] : dateStr).trim();
			String[] split = dateStr.split("[^\\d]");
			if(split.length > 2) {
				return LocalDateTime.of(
						Integer.parseInt(ArrayUtils.get(split, 0, "0")), 
						Integer.parseInt(ArrayUtils.get(split, 1, "0")), 
						Integer.parseInt(ArrayUtils.get(split, 2, "0")), 
						Integer.parseInt(ArrayUtils.get(split, 3, "0")), 
						Integer.parseInt(ArrayUtils.get(split, 4, "0")), 
						Integer.parseInt(ArrayUtils.get(split, 5, "0")));
			}
			String pattern = "yyyyMMddHHmmss" + (split.length == 1 ? "" : "." + String.join("", Collections.nCopies(split[1].length(), "S")));
			return parseDateTime(dateStr, pattern);
		} catch (Exception e) {
			log.error("parseDateTime方法请求参数=" + dateStr + ",转换为LocalDateTime异常");
			throw e;
		}
	}
	

	public static void main(String[] args) throws ParseException {
		System.out.println(splitMons(LocalDate.parse("2021-09-02"), LocalDate.parse("2022-01-01")));
		System.out.println(splitMons(LocalDate.parse("2022-01-02"), LocalDate.parse("2022-01-02")));
		System.out.println(splitMons(LocalDate.parse("2022-01-02"), LocalDate.parse("2022-02-02")));
		System.out.println(splitYears(LocalDate.parse("2022-01-02"), LocalDate.parse("2024-01-02")) + " " + splitYear(LocalDate.parse("2022-01-02"), LocalDate.parse("2024-01-02")));
		System.out.println(splitYears(LocalDate.parse("2022-01-02"), LocalDate.parse("2023-01-01")) + " " + splitYear(LocalDate.parse("2022-01-02"), LocalDate.parse("2023-01-01")));
		System.out.println(splitYears(LocalDate.parse("2022-01-02"), LocalDate.parse("2022-01-02")) + " " + splitYear(LocalDate.parse("2022-01-02"), LocalDate.parse("2022-01-02")));
		System.out.println(splitYears(LocalDate.parse("2022-01-02"), LocalDate.parse("2022-02-01")) + " " + splitYear(LocalDate.parse("2022-01-02"), LocalDate.parse("2022-02-01")));
		System.out.println("--------");
		System.out.println(collect("2022-01-02", "2022-01-04"));
		System.out.println(parseStr("20230101", "yyyy-MM-dd"));
		System.out.println(splitMons("2022-01-02", "2022-01-04"));
		System.out.println(splitMons("2022-01-02", "2023-01-04"));
		System.out.println(collectYear(LocalDate.parse("2022-01-02"), LocalDate.parse("2024-01-04")));
		System.out.println(DateUtils.splitMons("2023-06-30", "2023-07-01"));
		System.out.println(parseAnyDateTime("20240611102615"));
		System.out.println(parseAnyDateTime("20240611102615.342000+480  "));
		System.out.println(parseAnyDateTime("20240611102615.342000  "));// 
		System.out.println(parseAnyDateTime("2024-03-12 08:40") + "-------------------");
		System.out.println("LocalDateTime\r\n20240611105844.582000+480".replaceFirst("[\\s\\S]*?(\\d{4}.+\\d{2})[^\\d]*", "$1") + "----------333---------");
		System.out.println("         系统引导 2024-03-12 08:40\r\n".replaceFirst("[\\s\\S]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.println("         系统引导 2024-03-12 08:40".replaceFirst("[\\s\\S]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.println("绯荤粺寮曞2021-08-09 16:06\r\n".replaceFirst("[\\s\\S]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.println("绯荤粺寮曞2021-08-09 16:06".replaceFirst("[\\s\\S]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.print("-----------------------------------");
		System.out.println("         系统引导 2024-03-12 08:40\r\n".replaceFirst("[\\w\r\n ]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.println("         系统引导 2024-03-12 08:40".replaceFirst("[\\w\r\n ]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.println("绯荤粺寮曞2021-08-09 16:06\r\n".replaceFirst("[\\w\r\n ]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
		System.out.println("绯荤粺寮曞2021-08-09 16:06".replaceFirst("[\\w\r\n ]*?(\\d{4}.+\\d{2})[^\\d]*", "$1"));
	}
}
