/**
 * 
 */
package com.yeyeck.vertx.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yeyeck.vertx.util.DateUtils;
import com.yeyeck.vertx.util.NetUtil;
import com.yeyeck.vertx.util.TraceIdUtil;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.net.SocketAddress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Rest<T> implements Serializable {


	/**
	 * serialVersionUID:
	 */
	private static final long serialVersionUID = 1L;
	
	private String traceId = TraceIdUtil.traceIdNoLog();

	private String code = "200"; 

	private String msg = "";

	private T data;
	
	private String begTime;
	
	private String endTime;
	
	private long duration;
	
	private String ip;
	
	@JsonIgnore
	private transient String preLog;

	public Rest(String code, String msg) {
		this(code, msg, null);
	}
	
	public Rest(T data) {
		this("000000", "", data);
	}
	
	public Rest(String code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.endTime = DateUtils.ms();
	}
	
	public static Rest error(String msg) {
		return new Rest<>("500", msg, null);
	}
	
	public Rest<T> errorMsg(String msg) {
		setCode("500").setMsg(msg);
		return this;
	}

	public Rest<T> time() {
		LocalDateTime beginTime = DateUtils.parseDateTime(begTime, DateUtils.FMT_YYMDHMSS);
		return time(beginTime);
	}

	public Rest<T> time(LocalDateTime begTime) {
		return time(begTime, LocalDateTime.now());
	}
	public Rest<T> time(LocalDateTime begTime, LocalDateTime endTime) {
		setBegTime(DateUtils.format(begTime, DateUtils.FMT_YYMDHMSS));
		setEndTime(DateUtils.format(endTime, DateUtils.FMT_YYMDHMSS));
		setDuration(DateUtils.ms(endTime) - DateUtils.ms(begTime));
		return this;
	}
	
	public Rest<T> ip(HttpServerRequest req) {
		return setIp(NetUtil.getReqIp(req, false) + " -> " + NetUtil.getIp());
	}
	
	public String toString() {
		return Json.encode(this);
	}
}
