package com.ipaynow.yishouyun.goods.service

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.serviceproxy.ServiceBinder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val CONFIG_JDBC_URL = "goods.jdbc.url"
const val CONFIG_JDBC_DRIVER_CLASS = "goods.jdbc.driver_class"
const val CONFIG_GOODS_PROVIDER = "goods.provider"
const val CONFIG_USERNAME = "goods.username"
const val CONFIG_PASSWORD = "goods.password"

class GoodsDatabaseVerticle : AbstractVerticle() {

    private val log: Logger = LoggerFactory.getLogger(GoodsDatabaseVerticle::class.java)

    override fun start(startFuture: Future<Void>) {
        val sqlQueries = loadSqlQueries()
        val sqlClient = MySQLClient.createShared(
            vertx, JsonObject(
                mapOf(
                    "host" to config().getString(CONFIG_JDBC_URL, "localhost"),
                    "username" to config().getString(CONFIG_USERNAME, "root"),
                    "password" to config().getString(CONFIG_PASSWORD, "aggaer520"),
                    "database" to "yishouyun",
                    "port" to 3306
                )
            )
        )
        log.info("database init complete")
        GoodsDatabaseService.create(sqlClient, sqlQueries, Handler {
            if (it.succeeded()) {
                log.info("goods service register start")
                ServiceBinder(vertx)
                    .setAddress(CONFIG_GOODS_PROVIDER)
                    .register(GoodsDatabaseService::class.java, it.result())
                startFuture.complete()
            } else {
                log.error("接口代理：GoodsDatabaseService 注册失败：reason:${it.cause()}")
            }
        })
        log.info("goods service register complete")
    }

    private fun loadSqlQueries(): Map<SqlQuery, String> {
        return mapOf(
            SqlQuery.FETCH_STORE_GOODS to "select * from goods_info where store_id = ? and status > 0",
            SqlQuery.FETCH_GOODS_BY_CATEGORY to "select * from goods_info where store_id = ? and category_id = ? and status > 0",
            SqlQuery.FIND_GOODS_INFO to "select * from goods_info where id = ? and status > 0"
        )
    }
}