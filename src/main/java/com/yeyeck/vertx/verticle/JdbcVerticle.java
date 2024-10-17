
/**
 * Project Name: vertx-jpq-sample
 * File Name: JdbcVerticle.java
 * @date 2024年10月17日 21:15:33
 * Copyright (c) 2024 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.verticle;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.PropertiesUtil;

import com.yeyeck.vertx.enums.DbType;
import com.yeyeck.vertx.util.SqlUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.jdbc.JDBCClient;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO <br/>
 * @date 2024年10月17日 21:15:33
 * @author jiang
 * @version
 */
@Slf4j
public class JdbcVerticle extends AbstractVerticle {

	private PropertiesUtil properties;
	
	public JdbcVerticle(PropertiesUtil properties) {
		this.properties = properties;
	}
	
	public void initDb(Vertx vertx) {
		// jdbc:oracle:thin:@10.0.0.1:1521:orcl
		String url = properties.getStringProperty("datasource.dynamic.datasource.sale.url");
		if (StringUtils.isBlank(url)) {
			return;
		}
		String[] urls = url.split(":|@");
		String host = urls[4];
		int port = Integer.parseInt(urls[5]);
		String dbName = urls[6];
		String driver = properties.getStringProperty("datasource.dynamic.datasource.sale.driver-class-name");
		String acct = properties.getStringProperty("datasource.dynamic.datasource.sale.username");
		String pwd = properties.getStringProperty("datasource.dynamic.datasource.sale.password");
		String validQuerySql = properties.getStringProperty("datasource.dynamic.datasource.sale.druid.validationQuery");
		JDBCClient pool = SqlUtil.pool(vertx, url, driver, acct, pwd);
		pool.query(validQuerySql, ar -> {
			if (ar.succeeded()) {
				log.info("数据库连接成功：{}={}", validQuerySql, Json.encode(ar.result()));
			} else {
				log.error("数据库连异常：", ar.cause());
			}
		});
//		OraclePool poolOrcl = SqlUtil.poolOrcl(host, port, acct, pwd, dbName);
//		poolOrcl.preparedQuery(validQuerySql).execute(null);

		Promise<Integer> promiseConn = Promise.promise();
		SqlUtil.getConnection(DbType.ORCL).onSuccess(connection -> {
			Promise<Integer> promiseSql = Promise.promise();
			connection.preparedQuery(validQuerySql).execute(ar -> {
				if (ar.succeeded()) {
					promiseSql.complete(1);
				} else {
					promiseSql.fail(ar.cause());
				}
			});
			promiseSql.future().onSuccess(integer -> {
//				connection.close();
//				promiseConn.complete(integer);
				log.info("数据库连接成功：{}={}", validQuerySql, integer);
			}).onFailure(throwable -> {
//				connection.close();
//				promiseConn.fail(throwable);
				log.error("数据库连异常：", throwable);
			});
		});

//		Future<Integer> future = promiseConn.future();
	}
}

 