package com.yeyeck.vertx.service;

import com.yeyeck.vertx.dao.ArticleDao;
import com.yeyeck.vertx.enums.DbType;
import com.yeyeck.vertx.model.bo.ArticleBo;
import com.yeyeck.vertx.model.po.Article;
import com.yeyeck.vertx.util.SqlUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArticleService {

	private final ArticleDao articleDao = new ArticleDao();

	public Future<Integer> addArticle(ArticleBo articleFo) {
		Promise<Integer> promise = Promise.promise();
		Article article = articleFo.toArticle();
		SqlUtil.poolMysql().getConnection(ar -> {
			if (ar.succeeded()) {
				SqlConnection connection = ar.result();
				articleDao.add(connection, article).onSuccess(integer -> {
					promise.complete(integer);
					connection.close();
				}).onFailure(throwable -> {
					promise.fail(throwable);
					connection.close();
				});
			} else {
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}

	public Future<Article> getById(Integer id) {
		Promise<Article> promise = Promise.promise();
		SqlUtil.poolMysql().getConnection(as -> {
			if (as.succeeded()) {
				SqlConnection connection = as.result();
				articleDao.getById(connection, id).onSuccess(article -> {
					promise.complete(article);
					connection.close();
				}).onFailure(throwable -> {
					promise.fail(throwable);
					connection.close();
				});
			} else {
				promise.fail(as.cause());
			}
		});
		return promise.future();
	}

	public Future<Integer> update(Integer id, ArticleBo articleFo) {
		Promise<Integer> promise = Promise.promise();
		Article article = articleFo.toArticle();
		article.setId(id);
		SqlUtil.getConnection(DbType.MYSQL).onSuccess(connection -> {
			articleDao.update(connection, article).onSuccess(integer -> {
				connection.close();
				promise.complete(integer);
			}).onFailure(throwable -> {
				connection.close();
				promise.fail(throwable);
			});
		});
		return promise.future();
	}

	public Future<Integer> deleteById(Integer id) {
		Promise<Integer> promise = Promise.promise();
		SqlUtil.getConnection(DbType.MYSQL).onSuccess(connection -> {
			articleDao.deleteById(connection, id).onSuccess(res -> {
				// 正确执行sql, 释放connection
				connection.close();
				promise.complete(res);

			}).onFailure(throwable -> {
				// 执行sql发生错误， 释放connection
				connection.close();
				promise.fail(throwable);

			});
		}).onFailure(promise::fail); // 未拿到 connection
		return promise.future();
	}

	// Transaction Demo

	public Future<Integer> testTransaction() {
		Promise<Integer> promise = Promise.promise();
		Article article = new Article();
		article.setTitle("transaction");
		article.setAbstractText("transaction");
		article.setContent("transaction");
		article.setId(33);
		SqlUtil.getConnection(DbType.MYSQL).onSuccess(connection -> {
			// 开始一个transaction
			connection.begin(ar -> {
				if (ar.succeeded()) {
					// transaction 开启
					Transaction ts = ar.result();
					// 调用 dao 的方法执行SQL, 封装 dao 的方法都传入 connection 的就是为了实现事务
					articleDao.add(connection, article).onSuccess(integer -> articleDao.add(connection, article))
							.onSuccess(integer -> articleDao.add(connection, article))
							.onSuccess(integer -> articleDao.update(connection, article)).onSuccess(integer -> {
								// 都执行成功了才走到这里， 提交事务
								ts.commit(tsar -> {
									if (tsar.succeeded()) {
										promise.complete(1);
										connection.close();
									} else {
										promise.fail(tsar.cause());
									}
									connection.close();
								});
							}).onFailure(throwable -> {
								// 事务提交失败
								promise.fail(throwable);
								connection.close();
							});
				} else {
					// transaction 失败，关闭连接
					promise.fail(ar.cause());
					connection.close();
				}
			});
		});
		return promise.future();
	}
}
