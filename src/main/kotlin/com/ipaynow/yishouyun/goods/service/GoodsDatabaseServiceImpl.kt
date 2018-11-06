package com.ipaynow.yishouyun.goods.service

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNUSED_PARAMETER")
class GoodsDatabaseServiceImpl(
    sqLClient: SQLClient,
    private val sqlQueries: Map<SqlQuery, String>,
    readyHandler: Handler<AsyncResult<GoodsDatabaseService>>
) : GoodsDatabaseService {
    private val sqlClient: SQLClient = sqLClient

    private val log: Logger = LoggerFactory.getLogger(GoodsDatabaseServiceImpl::class.java)

    override fun fetchStoreGoods(
        storeId: String,
        resultHandler: Handler<AsyncResult<JsonArray>>
    ): GoodsDatabaseService {
        sqlClient.queryWithParams(sqlQueries[SqlQuery.FETCH_STORE_GOODS], JsonArray().add(storeId)) {
            if (it.failed()) {
                log.error("数据库连接失败，reason：${it.cause()}")
            } else {
                if (it.succeeded()) {
                    resultHandler.handle(Future.succeededFuture(JsonArray(it.result().results)))
                } else {
                    log.error("数据库查询失败:reason:{}", it.cause())
                }
            }
        }
        return this
    }

    override fun fetchGoodsByCategory(
        storeId: String,
        categoryId: Long,
        resultHandler: Handler<AsyncResult<JsonArray>>
    ): GoodsDatabaseService {
        sqlClient.queryWithParams(
            sqlQueries[SqlQuery.FETCH_GOODS_BY_CATEGORY],
            JsonArray().add(storeId).add(categoryId)
        ) {
            if (it.failed()) {
                log.error("【fetchGoodsByCategory】faild，reason：${it.cause()}")
                resultHandler.handle(Future.failedFuture(it.cause()))
            } else {
                resultHandler.handle(Future.succeededFuture(JsonArray(it.result().rows)))
            }
        }
        return this
    }

    override fun findGoodsInfo(
        goodsId: String,
        resultHandler: Handler<AsyncResult<JsonObject>>
    ): GoodsDatabaseService {
        sqlClient.queryWithParams(sqlQueries[SqlQuery.FIND_GOODS_INFO], JsonArray().add(goodsId)) {
            if (it.failed()) {
                log.error("【findGoodsInfo】faild，reason：${it.cause()}")
                resultHandler.handle(Future.failedFuture(it.cause()))
            } else {
                resultHandler.handle(Future.succeededFuture(it.result().toJson()))
            }
        }
        return this
    }

    init {
        readyHandler.handle(Future.succeededFuture(this))
    }
}