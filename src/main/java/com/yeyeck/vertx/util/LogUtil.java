
/**
 * Project Name: spcd-vertx-api
 * File Name: LogUtil.java
 * @date 2021-4-13 14:11:53
 * Copyright (c) 2021 .com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO <br/>
 * @date 2021-4-13 14:11:53
 * @author jiangpeiquan
 * @version
 */
public class LogUtil {
	private static Logger log = LoggerFactory.getLogger(LogUtil.class);

	public static void info(String format, Object... args) {
		detail(false, format, args);
	}
	
	// 异常放在最后一个参数！！！
	public static void error(String format, Object... args) {
		detail(true, format, args);
	}
	private static void detail(boolean isError, String format, Object... args) {
		String traceId = TraceIdUtil.traceId();
		Object object = ArrayUtils.isEmpty(args) ? null : args[args.length-1];
		boolean hasExcp = object instanceof Throwable;
		StackTraceElement callInfo = (isError && hasExcp) ? ((Throwable)object).getStackTrace()[1] : getCurStack(3);
		
		String msg = stackInfo(callInfo) + "[" + traceId + "] ";
		if(!isError) {
			log.info(msg + format, args);
		} else {
			log.error(msg + "[error]" + format, args);
		}
	}
	
	public static <T> T funcTime(Callable<T> callable) {
		long start = System.currentTimeMillis();
		T call = null;
		try {
			call = callable.call();
			long end = System.currentTimeMillis();
			log.info("{}[{}ms]返回结果={}", getPreThread(), end-start, call);
		} catch (Exception e) {
			log.error("{}执行异常：", getPreThread(), e);
		}
		return call;
	}
	
	public static StackTraceElement getCurStack() {
		return getCurStack(3);
	}
	public static StackTraceElement getCurStack(Integer idx) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if(null == idx || idx > (stackTrace.length - 1)) {
			return stackTrace[stackTrace.length - 1];
		}
		idx = idx > 0 ? idx : (stackTrace.length + idx);
		return stackTrace[idx];
	}
	public static String stackInfo(StackTraceElement stackTrace) {
		return String.format("[%s.%s-%d]", stackTrace.getClassName(), stackTrace.getMethodName(),
				stackTrace.getLineNumber());
	}

	/**
	 * 获取前一个调用的线程栈 <br/>
	 * @author jiangpeiquan
	 * @return
	 */
	public static String getPreThread() {
		return getCurThread(4);
	}
	public static String getCurThread() {
		return getCurThread(3);
	}
	public static String getCurThread(Integer idx) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		idx = idx == null ? (stackTrace.length - 1) : (idx > 0 ? idx : stackTrace.length + idx);
		return stackInfo(stackTrace[idx]);
	}
	
	/**
	 * 直接解析日志 <br/>
	 * @author jpq
	 * @param sqlLogs
	 * @return
	 */
	public static String buildSql(String sqlLogs) {
		int idx = sqlLogs.indexOf("Parameters");
		String sql = sqlLogs.substring(0, idx);
		int lastIdx = sql.lastIndexOf("\n");
		return buildSql(sql.substring(0, lastIdx), sqlLogs.substring(idx));
	}
	
    /**
     * 针对 sql 打印两行，组合在一起 <br/>
     * Preparing: SELECT * FROM asset_money_order WHERE (check_status = ? AND enable = ? AND (order_status = ? AND pay_off_status IN (?,?,?) OR (order_status IN (?,?))) AND due_date >= ?) ORDER BY id DESC LIMIT ?,?  
     * Parameters: 1(Integer), 1(Integer), 2(Integer), 3(Integer), 4(Integer), 5(Integer), 0(Integer), 3(Integer), 2021-08-17(String), 0(Long), 10(Long) 
     * @author jiangpeiquan
     * @param sqlPre
     * @param param
     * @return
     */
    public static String buildSql(String sqlPre, String param) {
    	if(StringUtils.isBlank(sqlPre)) {
    		return "";
    	}
    	
    	// 合并解决多行\r\n问题
    	String splitLine = sqlPre.contains("\r\n") ? "\r\n" : "\n";
    	sqlPre = Stream.of(sqlPre.split(splitLine)).collect(Collectors.joining());
    	param = Stream.of(param.split(splitLine)).collect(Collectors.joining());
    	sqlPre = sqlPre.trim();
    	sqlPre = StringUtils.substringAfter(sqlPre, "Preparing: ");
    	sqlPre = sqlPre.replaceAll("\\?", "{}");
    	param = StringUtils.substringAfter(param.trim(), "Parameters: ");
    	List<String> paramList = RegexUtil.getAllMatchGroups(param, "(null|.+?\\([A-Z]\\w+?\\)),? ?[\r\n]*?"); // 不能单纯用逗号，因为一些remark会有
    	String[] params = new String[paramList.size()];
    	String elem = null;
    	String type = null;
    	List<String> split = null;
    	for(int i=0; i<params.length; i++) {
    		elem = paramList.get(i);
    		if("null".equals(elem)) {
    			params[i] = elem;
    			continue;
    		}
    		split = RegexUtil.getAllMatchGroups(elem, "(.+)\\(([A-Z]\\w+)\\)");
    		elem = split.get(0);
    		type = split.get(1).trim().toLowerCase();
    		boolean isTime = type.contains("time"); // && elem.contains("T") oracle很多字符串型的时间类型，所以没有T;
    		if(type.startsWith("string") || isTime) {
    			if(isTime) {
    				elem = elem.replace("T", " ");
    			}
    			elem = "'" + elem + "'";
    		}
    		params[i] = elem;
    	}
    	return StringUtil.format(sqlPre, params);
    }
    
	public static void main(String[] args) {
		System.out.println(String.format("%011d", Integer.MAX_VALUE));
		System.out.println(String.format("%011d", Integer.MIN_VALUE));
		System.out.println(String.format("%05d", 123));
		System.out.println(String.format("%05d", -123));
//		String sqlLog = "Preparing: select * from (select distinct t.ID, t.BONDCODE, t.BONDABBR, t.TRD_SIDE trdSide, t.PRICETYPE priceType, t.PRICE, t.AMOUNT, to_char(t.TRD_DATE, 'yyyy-MM-dd') trdDate, t.CLEARING_SPEED clearingSpeed, t.TRD_TYPE trdType, t.AGENCY agency, t.COUNTERPARTY_ID counterpartyId, o.SHORTNAME counterparty, t.REALPARTY_ID realpartyId, r.SHORTNAME realparty, t.COUNTER_TRADER_ID counterTraderId, c1.NAME counterTrader, t.SELF_TRADER_ID selfTraderId, c2.NAME selfTrader, t.SELF_SEND selfSend, t.APPOINTMENT_NO appointmentNo, t.COUNTER_SEAT_NO counterSeatNo, t.COUNTER_TRADER_NO counterTraderNo, t.ACCOUNT_NO accountNo, t.STATUS status, t.OPERATOR_ID operatorId, u.NAME operator, b.TRADEMARKET, t.COMMIT_NO commitNo, t.ORIG_TEXT origText, t.IS_DF isDf, t.DF_NUM dfNum, to_char(t.CREATETIME, 'yyyy-MM-dd HH24:mi:ss') createTime, to_char(t.UPDATETIME, 'yyyy-MM-dd HH24:mi:ss') updateTime, t.EDITED_FIELDS editedFields, t.SELF_TRADER_NO selfTraderNo, t.ORIG_REALPARTY origRealparty, t.ORIG_COUNTERPARTY origCounterparty, t.IS_ADD_REALPARTY isAddRealparty, t.IS_ADD_COUNTERPARTY isAddCounterparty, t.BATCH_NO, to_char(t.SET_DATE, 'yyyy-MM-dd') setDate, t.AGENCY_FEE agencyFee, t.BRIDGE_FEE bridgeFee, t.NETPRICE netPrice, t.FULLPRICE fullPrice, t.YTM ytm, t.YTE yte, t.AI ai, t.TOTAL_AI totalAi, t.TRD_AMOUNT trdAmount, t.SET_AMOUNT setAmount, t.AGENCY_AMOUNT agencyAmount, t.MERGE_NO mergeNo from T_TRADE_PARSE t left join T_CFETS_AGENCIES o on t.COUNTERPARTY_ID = o.ID left join T_CFETS_AGENCIES r on t.REALPARTY_ID = r.ID left join T_CFETS_TRADERS c1 on t.COUNTER_TRADER_ID = c1.INNERID and c1.DELETEFLAG = 'N' left join T_CFETS_TRADERS c2 on t.SELF_TRADER_ID = c2.INNERID and c2.DELETEFLAG = 'N' and c2.AGENCYCODE = '100220' left join SY_USER u on t.OPERATOR_ID = u.ID left join T_BONDINFO b on t.BONDCODE = b.BONDCODE WHERE to_char(t.CREATETIME, 'yyyy-MM-dd') >= ? and to_char(t.CREATETIME, 'yyyy-MM-dd') <= ? and (b.TRADEMARKET = '1' or b.TRADEMARKET = '2') and ( ( t.operator_id in ( 10000243,10000246,10000248,10000253,10000260,10000280,10000282,10000283,10000320,10000380,10000381,10000423,10000440,10000180,10000321,10000247,10000300,10000249,10000252,10000301,10000245,10000403,10000424,10000442,10000240,10000241,10000242,10000250,10000251,10000255,10000256,10000257,10000281,10000340,10000360,10000254,10000382,10000420,10000421,10000422,10000441,10000322,10000323,10000386,10000400,6753,10000244,10000352,10000261,10000262,10000263,10000264,10000302,10000384,10000385,10000220,10000351,10000361,10000383,10000402,10000425,10000443,10000500,10000501,10000520,10000521,10000522,10000560,10000561,10000564,10000426,10000480,10000540,10000580,10000600,10000444,10000523,10000524,10000460,10000525,10000660,10000680 ) )) ) ORDER BY BATCH_NO desc nulls last, commitNo asc nulls last, trdSide\r\n"
//				+ "2021-11-08 16:57:21.556[1457633281745715200][http-nio-9091-exec-5]DEBUG c.h.f.s.m.m.T.queryTradeParse-137 - ==> Parameters: 2021-11-07(String), 2021-11-09(String)";
//		System.out.println(buildSql(sqlLog));
		
		System.out.println(buildSql(DocUtil.paste()));
//		System.out.println();
	}
} 