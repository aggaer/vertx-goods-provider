package com.ipaynow.yishouyun.goods

import com.ipaynow.yishouyun.goods.rest.HttpServerVerticle
import com.ipaynow.yishouyun.goods.service.GoodsDatabaseVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author jerry
 * @created on 2018/11/5
 */
@Suppress("unused")
class MainVerticle : AbstractVerticle() {
    private val log: Logger = LoggerFactory.getLogger(MainVerticle::class.java)

    override fun start(startFuture: Future<Void>) {
        val dbVerticleDeployment = Future.future<String>()
        vertx.deployVerticle(GoodsDatabaseVerticle(), dbVerticleDeployment.completer())
        dbVerticleDeployment.compose {
            log.info("db vertice id:$it")
            val httpVerticleDeployment = Future.future<String>()
            vertx.deployVerticle(HttpServerVerticle::class.java.name, httpVerticleDeployment.completer())
            httpVerticleDeployment
        }.setHandler { ar ->
            if (ar.succeeded()) {
                log.info("deploy success")
                startFuture.complete()
            } else {
                log.error("deploy faild:${ar.cause()}")
                startFuture.fail(ar.cause())
            }
        }
    }
}

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle())
}