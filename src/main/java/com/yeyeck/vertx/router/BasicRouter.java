package com.yeyeck.vertx.router;

import com.yeyeck.vertx.consts.BaseConst;

import io.vertx.ext.web.Router;

public interface BasicRouter extends BaseConst {
	
	void init(Router router);
}
