package com.yeyeck.vertx;

import org.apache.logging.log4j.util.PropertiesUtil;

import com.yeyeck.vertx.cfg.filter.CommonExceptionHandler;
import com.yeyeck.vertx.cfg.filter.TraceIdGenerateFilter;
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

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		long startTime = System.currentTimeMillis();
		PropertiesUtil propertiesUtil = new PropertiesUtil("application.properties");
		int port = propertiesUtil.getIntegerProperty("server.port", 8080);

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create()).handler(new TraceIdGenerateFilter()).failureHandler(new CommonExceptionHandler());
		// 创建一个http server 并将所有请求交给 router 来管理
		vertx.createHttpServer().requestHandler(router).listen(port, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				long endTime = System.currentTimeMillis();
				log.info("启动成功耗时{}ms： http://localhost:{}", endTime - startTime, port);
			} else {
				startPromise.fail(http.cause());
			}
		});
		// 在router上挂载url
		new ArticleRouter().init(router);
		new UserRouter().init(router);
	}

	
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainVerticle());
	}
}
