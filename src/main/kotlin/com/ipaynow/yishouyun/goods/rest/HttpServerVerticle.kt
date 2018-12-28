package com.ipaynow.yishouyun.goods.rest

import com.ipaynow.yishouyun.goods.service.CONFIG_GOODS_PROVIDER
import com.ipaynow.yishouyun.goods.service.GoodsDatabaseService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val CONFIG_HTTP_SERVER_PORT = "http.server.port"

class HttpServerVerticle : AbstractVerticle() {
    private val log: Logger = LoggerFactory.getLogger(HttpServerVerticle::class.java)
    private lateinit var goodsDbService: GoodsDatabaseService

    override fun start(startFuture: Future<Void>) {
        //通过服务地址创建db代理服务
        val goodsDbAddress = config().getString(CONFIG_GOODS_PROVIDER, "goods.provider")
        goodsDbService = GoodsDatabaseService.createProxy(vertx, goodsDbAddress)
        //创建http服务器
        val httpServer = vertx.createHttpServer()
        //定义路由规则
        val apiRouter = Router.router(vertx)
        val subRouter = Router.router(vertx)

        subRouter.get("/goods/:goodsId")
            .handler { goodsIndexHandler(it.request().getParam("goodsId"), it) }
        subRouter.get("/stores/:storeId/categories/:categoryId/goods/")
            .handler { ar ->
                val storeId = ar.request().getParam("storeId")
                val categoryId = ar.request().getParam("categoryId").toLong()
                goodsByCategoryIdHandler(storeId, categoryId, ar)
            }
        subRouter.get("/stores/:storeId/goods")
            .handler { ar -> goodsByStoreIdHandler(ar.request().getParam("storeId"), ar) }

        apiRouter.mountSubRouter("/api", subRouter)

        val serverPort = config().getInteger(CONFIG_HTTP_SERVER_PORT, 18082)
        httpServer
            .requestHandler(apiRouter::accept)
            .listen(serverPort) { ar ->
                if (ar.succeeded()) {
                    log.info("服务器启动成功， port： $serverPort")
                    startFuture.succeeded()
                } else {
                    log.error("服务器启动失败，reason:${ar.cause()}")
                }
            }
    }

    private fun goodsIndexHandler(goodsId: String, rc: RoutingContext) {
        goodsDbService.findGoodsInfo(goodsId, Handler { ar ->
            if (ar.succeeded()) {
                val result = ar.result()
                rc.response().putHeader("Content-Type", "application/json")
                rc.response().end(result.toBuffer())
            } else {
                rc.fail(ar.cause())
            }
        })
    }

    private fun goodsByCategoryIdHandler(storeId: String, categoryId: Long, rc: RoutingContext) {
        goodsDbService.fetchGoodsByCategory(storeId, categoryId, Handler { ar ->
            if (ar.succeeded()) {
                val result = ar.result()
                rc.response().putHeader("Content-Type", "application/json")
                rc.response().end(result.toString())
            } else {
                rc.fail(ar.cause())
            }
        })
    }

    private fun goodsByStoreIdHandler(storeId: String, rc: RoutingContext) {
        goodsDbService.fetchStoreGoods(storeId, Handler { ar ->
            if (ar.succeeded()) {
                val result = ar.result()
                rc.response().putHeader("Content-Type", "application/json")
                rc.response().end(result.toBuffer())
            } else {
                rc.fail(ar.cause())
            }
        })
    }
}