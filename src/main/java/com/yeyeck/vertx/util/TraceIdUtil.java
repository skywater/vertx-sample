
/**
 * File Name: TraceIdUtil.java
 * @date 2021-3-4 14:55:24
 * Copyright (c) 2021 .com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import org.apache.commons.lang3.StringUtils;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * TODO <br/>
 * @date 2021-3-4 14:55:24
 * @author jiangpeiquan
 * @version
 */
public class TraceIdUtil extends ClassicConverter {
	private static Logger log = LoggerFactory.getLogger(TraceIdUtil.class);
	
	private static final ThreadLocal<String> TRACE_ID_LOCAL = new ThreadLocal<>();
	public static final String TRACE_ID = "traceId";


	public static Long genId() {
		return SnowflakeIdGenerator.getId();
	}
	
	public static String genTraceId() {
		String traceId = SnowflakeIdGenerator.getIdStr();
		log.info("{}设置新生成traceId={}", LogUtil.getPreThread(), traceId);
		setId(traceId);
	    return traceId;
	}
	
	public static String genTraceId(boolean isLog) {
		String traceId = SnowflakeIdGenerator.getIdStr();
		if(isLog) {
			log.info("{}设置新生成traceId={}", LogUtil.getPreThread(), traceId);
		}
		setId(traceId);
	    return traceId;
	}
	
	/**
	 * 函数式生成，打印调用函数的方法，注意：该方法是生成表id的所以不能重复（即，traceId()方法），也不能覆盖当前traceId <br/>
	 * @author jpq
	 * @return
	 */
	public static String genTableId() {
		String traceId = SnowflakeIdGenerator.getIdStr();
		log.info("{}生成表主键Id={}", LogUtil.getCurThread(5), traceId);
		traceIdIfMiss(traceId);
	    return traceId;
	}
	
	/**
	 * 获取traceId，没有则新生成 <br/>
	 * @author jiangpeiquan
	 * @return
	 */
	public static String traceId() {
		String traceId = getCurId(false);
		boolean isBlank = StringUtils.isBlank(traceId);
		traceId = isBlank ? genTraceId(false) : traceId;
		log.info("{}{}traceId={}", LogUtil.getPreThread(), isBlank ? "设置新生成" : "获取当前", traceId);
		return traceId;
	}

	public static String traceIdNoLog() {
		String traceId = getCurId(false);
		return StringUtils.isBlank(traceId) ? genTraceId(true) : traceId;
	}
	
	/**
	 * 设置当前traceId并返回，有则设置，否则新生成设置，用于当前traceId在循环中手动清除的情况 <br/>
	 * @author jiangpeiquan
	 * @return
	 */
	public static String traceId(String traceId) {
		boolean isBlank = StringUtils.isBlank(traceId);
		traceId = isBlank ? genTraceId(false) : setTraceIdNoLog(traceId);
		log.info("{} {}traceId", LogUtil.getPreThread(), isBlank ? "设置新生成" : "直接设置");
		return traceId;
	}
	public static String traceId(String traceId, String info) {
		boolean isBlank = StringUtils.isBlank(traceId);
		traceId = isBlank ? genTraceId(false) : setTraceIdNoLog(traceId);
		log.info("{} {} 任务开始执行，{}traceId", LogUtil.getPreThread(), info, isBlank ? "设置新生成" : "直接设置");
		return traceId;
	}
	
	/**
	 * 当前线程没有则新生成赋值，有则不覆盖 <br/>
	 * @author jpq
	 * @param traceId
	 * @return
	 */
	public static String traceIdIfMiss(String traceId) {
		String id = getCurId(false);
		if(StringUtils.isNotBlank(id)) {
			return id;
		}
		return setTraceIdNoLog(traceId);
	}
	
//	public static String getCurId() {
//		String id = getSetId();
//		log.info("{}获取当前traceId={}", LogUtil.getPreThread(), id);
//	    return id;
//	}
	public static String getCurId(boolean isLog) {
		String id = getSetId();
		if(isLog) {
			log.info("{}获取当前traceId={}", LogUtil.getPreThread(), id);
		}
	    return id;
	}
	
	/**
	 * 设置seata的xid为traceId <br/>
	 * @author jpq
	 * @param xid
	 * @return
	 */
	public static String setXid(String xid) {
		if(StringUtils.isBlank(xid)) {
			return traceId();
		}
		int idx = xid.lastIndexOf(":");
		return setTraceId(xid.substring(idx + 1));
	}
	
	public static String setTraceId(String traceId) {
		log.info("{}设置当前traceId={}开始", LogUtil.getPreThread(), traceId);
		if(StringUtils.isBlank(traceId)) {
			traceId = traceId();
		}
		setId(traceId);
		log.info("{}设置当前traceId={}结束", LogUtil.getPreThread(), traceId);
		return traceId;
	}
	
	public static String setTraceIdNoLog(String traceId) {
		if(StringUtils.isBlank(traceId)) {
			traceId = traceId();
		}
		setId(traceId);
		return traceId;
	}
	
	public static void clear() {
		log.info("{}清除 traceId", LogUtil.getPreThread());
		TRACE_ID_LOCAL.remove();
		MDC.remove(TRACE_ID);
	}
	
	private static String getSetId() {
		String id = getId();
		if(StringUtils.isNotBlank(id)) {
			setId(id);
		}
	    return id;
	}

	private static String getId() {
//		setTid();
		String id = TRACE_ID_LOCAL.get();
		if(StringUtils.isBlank(id)) {
			id = MDC.get(TRACE_ID);
		}
		return id;
	}
	private static void setId(String id) {
//		setTid();
		TRACE_ID_LOCAL.set(id);
		MDC.put(TRACE_ID, id);
	}
	
	public static void main(String[] args) {
//		System.out.println(traceId());
		String xid = "10.0.218.192:8091:3251788173402289608";
		System.out.println(xid.substring(xid.lastIndexOf(":")+1));
	}

	@Override
	public String convert(ILoggingEvent event) {
		return traceIdNoLog();
	}
	
//	public static void setTid() {
//		String tid = TraceContext.traceId();
//		MDC.put("tid", StringUtils.defaultIfBlank(tid, "XTID:"));
//	}
}