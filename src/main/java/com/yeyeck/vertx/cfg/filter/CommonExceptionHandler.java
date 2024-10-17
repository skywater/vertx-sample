package com.yeyeck.vertx.cfg.filter;

import org.apache.commons.lang3.StringUtils;

import com.yeyeck.vertx.consts.BaseConst;
import com.yeyeck.vertx.model.Rest;
import com.yeyeck.vertx.util.TraceIdUtil;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonExceptionHandler implements Handler<RoutingContext>, BaseConst {

	@Override
	public void handle(RoutingContext ctx) {
		HttpServerResponse resp = ctx.response();
		Rest<Object> rest = getRestObj(ctx);
		Object data = popData(ctx, REST_BODY);
		Throwable failure = ctx.failure();
		boolean isValid = failure instanceof ValidationException;
		String err = (isValid ? "校验" : "") + "异常：";
		String msg = err + StringUtils.defaultIfBlank(failure.getMessage(), failure.toString());
		String ret = rest.time().setData(data).errorMsg(msg).toString();
		resp.end(ret);
		log.info("------ {},返回参数={},{}", rest.getPreLog(), ret, err, failure);
		TraceIdUtil.clear();
	}
}
