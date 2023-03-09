package jun.won.responsedsl.reative


import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import jun.won.responsedsl.model.Dummy
import kotlinx.coroutines.flow.asFlow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.coRouter

class WebClientServletDslTest : FunSpec({

    val ok_json_with_custom_header =
        makeDefaultResponse(HttpStatus.OK, MediaType.APPLICATION_JSON, mapOf("X-TEST-HEADER" to "TEST-VALUE"))

    val router = coRouter {
        GET("/dsl") {
            response {
                status { HttpStatus.OK }
                contentType { MediaType.TEXT_PLAIN }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
                bodyValue { "Hello World" }
            }
        }
        GET("/dsl/flow") {
            response {
                status { HttpStatus.OK }
                contentType { MediaType.APPLICATION_JSON }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
                body {
                    listOf(
                        Dummy("Hello", 0),
                        Dummy("World", 1)
                    ).asFlow()
                }
            }
        }
    }

    val strategies = HandlerStrategies.builder()
        .codecs { configurer ->
            configurer.defaultCodecs()
                .jackson2JsonEncoder(Jackson2JsonEncoder(ObjectMapper(), MediaType.APPLICATION_JSON))
            configurer.defaultCodecs()
                .jackson2JsonDecoder(Jackson2JsonDecoder(ObjectMapper(), MediaType.APPLICATION_JSON))
        }.build()

    val client = WebTestClient
        .bindToRouterFunction(router)
        .handlerStrategies(strategies)
        .build()

    test("DSL Path generates the same result as Fluent API.") {
        client.get()
            .uri("/dsl")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-TEST-HEADER", "TEST-VALUE")
            .expectBody(String::class.java)
            .isEqualTo("Hello World")
    }

    test("DSL Path generates the same result as Fluent API. (Flow)") {
        client.get()
            .uri("/dsl/flow")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-TEST-HEADER", "TEST-VALUE")
            .expectBodyList(Dummy::class.java)
            .hasSize(2)
    }
})
