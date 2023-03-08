package jun.won.responsedsl.webflux

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait


data class Dummy(val name: String, val age: Int)
class WebfluxDslTest : FunSpec({
    test("DSL generates the same result as Fluent API.") {
        val fluentApi = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.TEXT_PLAIN)
                .header("X-TEST-HEADER", "TEST-VALUE")
                .bodyValueAndAwait("Hello World")
        }

        val dsl = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            response {
                status { HttpStatus.OK }
                contentType { MediaType.TEXT_PLAIN }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
                bodyValue { "Hello World" }
            }
        }

        val responseEntity: String = (fluentApi as EntityResponse<String>).entity()
        val dslEntity: String = (dsl as EntityResponse<String>).entity()

        dsl.statusCode() shouldBe fluentApi.statusCode()
        dsl.headers().toMap() shouldBe fluentApi.headers().toMap()
        dslEntity shouldBe responseEntity
    }

    test("flow Type DSL") {
        val fluentApi = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.TEXT_PLAIN)
                .header("X-TEST-HEADER", "TEST-VALUE")
                .bodyAndAwait(
                    flow {
                        emit("Hello World")
                        emit("Hello World")
                    }
                )
        }

        val dsl = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            response {
                status { HttpStatus.OK }
                contentType { MediaType.TEXT_PLAIN }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
                body {
                    flow {
                        emit("Hello World")
                        emit("Hello World")
                    }
                }
            }
        }

        val responseEntity: List<String> = (fluentApi as EntityResponse<Flow<String>>).entity().toList()
        val dslEntity: List<String> = (dsl as EntityResponse<Flow<String>>).entity().toList()

        dsl.statusCode() shouldBe fluentApi.statusCode()
        dsl.headers().toMap() shouldBe fluentApi.headers().toMap()
        dslEntity shouldBe responseEntity
        println(dslEntity)
    }

    test("Class and reified") {
        val dsl = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            response {
                status { HttpStatus.OK }
                contentType { MediaType.TEXT_PLAIN }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
                body {
                    flow {
                        emit("Hello World")
                        emit("Hello World")
                    }
                }
            }
        }

        val dsl2 = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            response {
                status { HttpStatus.OK }
                contentType { MediaType.TEXT_PLAIN }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
                body(String::class.java) {
                    flow {
                        emit("Hello World")
                        emit("Hello World")
                    }
                }
            }
        }

        val dslEntity: List<String> = (dsl as EntityResponse<Flow<String>>).entity().toList()
        val dslEntity2: List<String> = (dsl2 as EntityResponse<Flow<String>>).entity().toList()

        dslEntity shouldBe dslEntity2
    }

})
