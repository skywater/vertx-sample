
/**
 * Project Name: vertx-jpq-sample
 * File Name: BaseConst.java
 * @date 2024年10月17日 13:22:47
 * Copyright (c) 2024 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.consts;

import com.yeyeck.vertx.model.Rest;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * TODO <br/>
 * @date 2024年10月17日 13:22:47
 * @author jiang
 * @version
 */
public interface BaseConst {
	
	String AUTHORIZATION = "Authorization";
	
	String REQ_ID = "reqId";
	
	String BEG_TIME = "begTime";
	
	String REST_OBJ = "restObj";  // Rest<T>
	
	String REST_BODY = "restBody";  // <T>
	
	default void pushData(RoutingContext ctx, String key, Object obj) {
		ctx.data().put(key, obj);
	}
	
	default <T> void pushRestObj(RoutingContext ctx, Rest<T> obj) {
		ctx.data().put(REST_OBJ, obj);
	}
	
	default void pushBody(RoutingContext ctx, Object obj) {
		ctx.data().put(REST_BODY, obj);
	}
	
	default <T> Rest<T> getRestObj(RoutingContext ctx) {
		return getData(ctx, Rest.class, REST_OBJ);
	}
	
	default Object getData(RoutingContext ctx, String key) {
		return ctx.data().get(key);
	}
	
	default Object popData(RoutingContext ctx, String key) {
		return ctx.data().remove(key);
	}

	default <T> T getData(RoutingContext ctx, Class<T> clazz, String key) {
		return getData(ctx, clazz, key, false);
	}
	default <T> T getData(RoutingContext ctx, Class<T> clazz, String key, boolean isPop) {
		Object data = isPop ? popData(ctx, key) : getData(ctx, key);
		if(null == data) {
			return null;
		} else if(clazz.isAssignableFrom(data.getClass())) {
			return clazz.cast(data);
		} else {
			String encode = Json.encode(data);
			return Json.decodeValue(encode, clazz);
		}
	}

}

 