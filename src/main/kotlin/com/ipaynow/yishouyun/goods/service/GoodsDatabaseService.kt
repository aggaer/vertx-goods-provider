package com.ipaynow.yishouyun.goods.service

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.GenIgnore
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient

@VertxGen
@ProxyGen
interface GoodsDatabaseService {
    @Fluent
    fun fetchStoreGoods(storeId: String, resultHandler: Handler<AsyncResult<JsonArray>>): GoodsDatabaseService

    @Fluent
    fun findGoodsInfo(goodsId: String, resultHandler: Handler<AsyncResult<JsonObject>>): GoodsDatabaseService

    @Fluent
    fun fetchGoodsByCategory(
        storeId: String,
        categoryId: Long,
        resultHandler: Handler<AsyncResult<JsonArray>>
    ): GoodsDatabaseService

    @GenIgnore
    companion object {
        fun create(
            sqlClient: SQLClient,
            sqlQueries: Map<SqlQuery, String>,
            readyHandler: Handler<AsyncResult<GoodsDatabaseService>>
        ): GoodsDatabaseService {
            return GoodsDatabaseServiceImpl(sqlClient, sqlQueries, readyHandler)
        }

        fun createProxy(vertx: Vertx, address: String): GoodsDatabaseServiceVertxEBProxy {
            return GoodsDatabaseServiceVertxEBProxy(vertx, address)
        }
    }
}