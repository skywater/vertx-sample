package com.yeyeck.vertx.router;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;

public class UserRouter implements BasicRouter {

	@Override
	public void init(Router router) {
		HTTPRequestValidationHandler validName = HTTPRequestValidationHandler.create()
				.addQueryParam("name", ParameterType.GENERIC_STRING, true)  // name必传，否则报错
				.addQueryParam("id", ParameterType.INT, false);
		HTTPRequestValidationHandler validId = HTTPRequestValidationHandler.create().addPathParam("id", ParameterType.GENERIC_STRING);
		router.get("/user/getByName").handler(validName).handler(this::getByName);
		router.get("/user/:id").handler(validId).handler(this::getById); // 这种写法要放最后，不然会接收所有方法
	}

	private void getById(RoutingContext routingContext) {
		Integer id = Integer.parseInt(routingContext.request().getParam("id"));
		JsonObject ret = new JsonObject().put("id", id).put("name", "小明s");
		pushBody(routingContext, ret);
	}

	private void getByName(RoutingContext routingContext) {
		HttpServerRequest request = routingContext.request();
		String name = request.getParam("name");
		JsonObject ret = new JsonObject().put("id", name.hashCode()).put("name", name);
		pushBody(routingContext, ret);
	}
}
