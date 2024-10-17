package com.yeyeck.vertx.util;

import com.yeyeck.vertx.enums.DbType;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
//import io.vertx.oracleclient.OracleConnectOptions;
//import io.vertx.oracleclient.OraclePool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;

public class SqlUtil {
	private static MySQLPool poolMysql;
//	private static OraclePool poolOrcl;
	
	static {
		poolMysql = poolMysql("47.106.185.245", 3306, "root", "cckk00522", "vertx");
//		poolOrcl = poolOrcl("47.106.185.245", 3306, "root", "cckk00522", "vertx");
	}

	private SqlUtil() {
	}

	public static MySQLPool poolMysql() {
		return poolMysql;
	}

//	public static OraclePool poolOrcl() {
//		return poolOrcl;
//	}
	
	public static MySQLPool poolMysql(String host, int port, String acct, String pwd, String dbName){
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setHost(host).setUser(acct)
				.setPassword(pwd).setPort(port).setDatabase(dbName);
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
		poolMysql = MySQLPool.pool(Vertx.vertx(), connectOptions, poolOptions);
		return poolMysql;
	}
	
//	public static OraclePool poolOrcl(String host, int port, String acct, String pwd, String dbName){
//		OracleConnectOptions connectOptions = new OracleConnectOptions().setHost(host).setUser(acct)
//				.setPassword(pwd).setPort(port).setDatabase(dbName);
//		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
//		poolOrcl = OraclePool.pool(Vertx.vertx(), connectOptions, poolOptions);
//		return poolOrcl;
//	}

	public static Future<SqlConnection> getConnection(DbType dbType) {
		Promise<SqlConnection> promise = Promise.promise();
		Pool pool = poolMysql; // DbType.MYSQL == dbType ? poolMysql : poolOrcl;
		pool.getConnection(ar -> {
			if (ar.succeeded()) {
				promise.complete(ar.result());
			} else {
				ar.cause().printStackTrace();
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}
	

	public static JDBCClient pool(Vertx vertx, String url, String driver, String acct, String pwd) {
		JsonObject dbConfig = new JsonObject();
		dbConfig.put("url", url);
		dbConfig.put("driver_class", driver);
		dbConfig.put("user", acct);
		dbConfig.put("password", pwd);
		JDBCClient dbClient = JDBCClient.createShared(vertx, dbConfig);
		return dbClient;
	}
}
