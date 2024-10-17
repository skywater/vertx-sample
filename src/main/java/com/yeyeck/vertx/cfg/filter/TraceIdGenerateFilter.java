
/**
 * Project Name: vertx-jpq-sample
 * File Name: TraceIdGenerateFilter.java
 * @date 2024年10月17日 11:20:32
 * Copyright (c) 2024 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.cfg.filter;

import com.yeyeck.vertx.consts.BaseConst;
import com.yeyeck.vertx.model.Rest;
import com.yeyeck.vertx.util.DateUtils;
import com.yeyeck.vertx.util.NetUtil;
import com.yeyeck.vertx.util.TraceIdUtil;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO <br/>
 * @date 2024年10月17日 11:20:32
 * @author jiang
 * @version
 */
@Slf4j
public class TraceIdGenerateFilter implements Handler<RoutingContext>, BaseConst {

	@Override
	public void handle(RoutingContext ctx) {
		HttpServerRequest request = ctx.request();
		String token = request.getHeader(AUTHORIZATION);
		String reqId = request.getHeader(REQ_ID);
		String uri = NetUtil.getRequestUrl(request);
		String reqJson = ctx.getBodyAsString();
		String traceId = request.getHeader(TraceIdUtil.TRACE_ID);
		TraceIdUtil.traceId(traceId);
		Rest<Object> rest = new Rest<>().setBegTime(DateUtils.ms()).ip(request);
		String clientIp = NetUtil.getReqIp(request, false);
		String preLog = String.format("------ 接收到请求 clientIp=%s,token=%s,reqId=%s,url=%s,请求参数=%s", clientIp, token, reqId, uri, reqJson);
		rest.setPreLog(preLog);
		pushRestObj(ctx, rest);
		HttpServerResponse resp = ctx.response();
		resp.putHeader("Content-Type", "application/json; charset=UTF-8");
		
		ctx.next();

		Object data = popData(ctx, REST_BODY);
		String ret = rest.setData(data).time().toString();
		resp.end(ret);
		log.info("------ {},返回参数={} ------", ret);
		TraceIdUtil.clear();
	}

}