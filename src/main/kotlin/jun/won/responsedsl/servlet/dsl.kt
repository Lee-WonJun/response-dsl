package jun.won.responsedsl.servlet
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerResponse

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

class ResponseBuilder {
    private var status: HttpStatus = HttpStatus.OK
    private var contentType: MediaType? = null
    private var headersMap: MutableMap<String, String> = mutableMapOf()
    private var body: Any? = null

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


    fun body(init: () -> Any?) {
        body = init()
    }

    private fun ServerResponse.BodyBuilder.bodyBuild(body: Any?): ServerResponse {
        return if (body != null) {
            this.body(body)
        } else {
            this.build()
        }
    }

    private fun ServerResponse.BodyBuilder.headers(headersMap: Map<String,String>): ServerResponse.BodyBuilder {
        headersMap.forEach { (header, value) -> this.header(header, value) }
        return this
    }

    private fun ServerResponse.BodyBuilder.contentType(contentType: MediaType?): ServerResponse.BodyBuilder {
        if (contentType != null) {
            this.header(HttpHeaders.CONTENT_TYPE, contentType.toString())
        }
        return this
    }

    fun build(): ServerResponse {
        return ServerResponse.status(status)
            .contentType(contentType)
            .headers(headersMap)
            .bodyBuild(body)
    }
}

fun response(init: ResponseBuilder.() -> Unit): ServerResponse {
    val builder = ResponseBuilder()
    builder.init()
    return builder.build()
}

fun makeDefaultResponse(status: HttpStatus , contentType:MediaType? = null, headersMap: Map<String, String>? = null): (init:ResponseBuilder.()-> Unit) -> ServerResponse {
    return { init ->
        response {
            this.status { status }
            contentType?.let { this.contentType { it } }
            headersMap?.let { this.headers { it.forEach { header { it.key to it.value } } } }
            init()
        }
    }
}
