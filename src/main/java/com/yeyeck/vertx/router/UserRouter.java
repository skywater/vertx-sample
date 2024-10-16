package com.yeyeck.vertx.router;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class UserRouter extends BasicRouter {

	@Override
	public void init(Router router) {
		router.get("/user/:id").handler(this::getById);
	}

	private void getById(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		Integer id = Integer.parseInt(routingContext.request().getParam("id"));
		response.end(new JsonObject().put("id", id).put("name", "小明s").encode().toString());
	}
}
