package com.yeyeck.vertx;

import org.apache.logging.log4j.util.PropertiesUtil;

import com.yeyeck.vertx.router.ArticleRouter;
import com.yeyeck.vertx.router.UserRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainVerticle());
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		PropertiesUtil propertiesUtil = new PropertiesUtil("application.properties");
		int port = propertiesUtil.getIntegerProperty("server.port", 8080);
		// 创建一个 router
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create()).handler(ctx -> {
			ctx.response().putHeader("Content-Type", "application/json; charset=UTF-8");
			ctx.next();
		});
		// 创建一个http server 并将所有请求交给 router 来管理
		vertx.createHttpServer().requestHandler(router).listen(port, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				log.info("启动成功： http://localhost:{}", port);
			} else {
				startPromise.fail(http.cause());
			}
		});
		// 在router上挂载url
		new ArticleRouter().init(router);
		new UserRouter().init(router);

	}
}
