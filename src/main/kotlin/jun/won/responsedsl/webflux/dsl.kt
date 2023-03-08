package jun.won.responsedsl.webflux

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

/*
    * This file contains the DSL for building ServerResponse objects.
* */

class HeadersBuilder {
    private var headers: MutableMap<String, String> = mutableMapOf()

    operator fun (() -> Pair<String, String>).unaryPlus() {
        headers[this().first] = this().second
    }

    fun header(init: () -> Pair<String, String>) {
        headers[init().first] = init().second
    }

    fun build(): Map<String, String> {
        return headers
    }
}

class ResponseBuilder<T : Any> {
    private var status: HttpStatus = HttpStatus.OK
    private var contentType: MediaType? = null
    private var headersMap: MutableMap<String, String> = mutableMapOf()
    private var bodyValue: T? = null
    public var body: Flow<T>? = null
    public var bodyType: Class<*>? = null

    fun status(init: () -> HttpStatus) {
        status = init()
    }

    fun contentType(init: () -> MediaType) {
        contentType = init()
    }

    fun headers(init: HeadersBuilder.() -> Unit) {
        val builder = HeadersBuilder()
        builder.init()
        headersMap += builder.build().toMutableMap()
    }

    fun header(init: () -> Pair<String, String>) {
        headersMap[init().first] = init().second
    }

    fun bodyValue(init: () -> T?) {
        bodyValue = init()
    }

    fun body(Type: Class<T>, init: () -> Flow<T>?) {
        body = init()
        bodyType = Type
    }

    inline fun <reified R : T> body(noinline init: () -> Flow<R>?) {
        body = init()
        bodyType = R::class.java
    }

    private fun ServerResponse.BodyBuilder.headers(headersMap: Map<String, String>): ServerResponse.BodyBuilder {
        headersMap.forEach { (header, value) -> this.header(header, value) }
        return this
    }

    private fun ServerResponse.BodyBuilder.contentType(contentType: MediaType?): ServerResponse.BodyBuilder {
        if (contentType != null) {
            this.header(HttpHeaders.CONTENT_TYPE, contentType.toString())
        }
        return this
    }

    private suspend fun ServerResponse.BodyBuilder.bodyAndAwaitClass(flow: Flow<T>, Type: Class<T>): ServerResponse {
        return body(flow, Type).awaitSingle()
    }


    private suspend fun ServerResponse.BodyBuilder.bodyBuild(): ServerResponse {
        return if (bodyValue != null) {
            this.bodyValueAndAwait(bodyValue!!)
        } else if (body != null && bodyType != null) {
            this.bodyAndAwaitClass(body!!, bodyType!! as Class<T>)
        } else {
            this.buildAndAwait()
        }
    }

    suspend fun build(): ServerResponse {
        return ServerResponse.status(status)
            .contentType(contentType)
            .headers(headersMap)
            .bodyBuild()
    }
}

suspend fun <T : Any> response(init: ResponseBuilder<T>.() -> Unit): ServerResponse {
    val builder = ResponseBuilder<T>()
    builder.init()
    return builder.build()
}

fun makeDefaultResponse(
    status: HttpStatus,
    contentType: MediaType? = null,
    headersMap: Map<String, String>? = null
): suspend (init: ResponseBuilder<Any>.() -> Unit) -> ServerResponse {
    return { init ->
        response<Any> {
            this.status { status }
            contentType?.let { this.contentType { it } }
            headersMap?.let { this.headers { it.forEach { header { it.key to it.value } } } }
            init()
        }
    }
}
