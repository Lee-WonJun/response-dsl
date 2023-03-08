package jun.won.responsedsl.servlet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.EntityResponse
import org.springframework.web.servlet.function.ServerResponse

data class Dummy(val name: String, val age: Int)
class ServletDslTest : FunSpec({
    test("DSL generates the same result as Fluent API.") {
        val fluentApi = ServerResponse.status(HttpStatus.OK)
            .contentType(MediaType.TEXT_PLAIN)
            .header("X-TEST-HEADER", "TEST-VALUE")
            .body("Hello World")

        val dsl = response {
            status { HttpStatus.OK }
            contentType { MediaType.TEXT_PLAIN }
            header { "X-TEST-HEADER" to "TEST-VALUE" }
            body { "Hello World" }
        }

        val responseEntity: String = (fluentApi as EntityResponse<String>).entity()
        val dslEntity: String = (dsl as EntityResponse<String>).entity()

        dsl.statusCode() shouldBe fluentApi.statusCode()
        dsl.headers().toMap() shouldBe fluentApi.headers().toMap()
        dslEntity shouldBe responseEntity
    }

    test("Header builder works.") {
        val fluentApi = ServerResponse.status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header("X-TEST-HEADER", "TEST-VALUE")
            .body(Dummy("Jun", 25))

        val dsl = response {
            status { HttpStatus.OK }
            headers {
                header { HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
            }
            body { Dummy("Jun", 25) }
        }

        val responseEntity: Dummy = (fluentApi as EntityResponse<Dummy>).entity()
        val dslEntity: Dummy = (dsl as EntityResponse<Dummy>).entity()

        dsl.statusCode() shouldBe fluentApi.statusCode()
        dsl.headers().toMap() shouldBe fluentApi.headers().toMap()
        dslEntity shouldBe responseEntity
    }

    test("set Default Dsl Function via makeDefaultResponse") {
        val ok_json =
            makeDefaultResponse(HttpStatus.OK, MediaType.APPLICATION_JSON, mapOf("X-TEST-HEADER" to "TEST-VALUE"))
        val dsl = ok_json {
            body { Dummy("Jun", 25) }
        }

        val fluentApi = ServerResponse.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-TEST-HEADER", "TEST-VALUE")
            .body(Dummy("Jun", 25))


        val responseEntity: Dummy = (fluentApi as EntityResponse<Dummy>).entity()
        val dslEntity: Dummy = (dsl as EntityResponse<Dummy>).entity()

        dsl.statusCode() shouldBe fluentApi.statusCode()
        dsl.headers().toMap() shouldBe fluentApi.headers().toMap()
        dslEntity shouldBe responseEntity
    }

    test("unaryPlus works") {
        val usedHeaderBuilder = response {
            status { HttpStatus.OK }
            headers {
                header { HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE }
                header { "X-TEST-HEADER" to "TEST-VALUE" }
            }
            body { Dummy("Jun", 25) }
        }

        val usedUnaryPlus = response {
            status { HttpStatus.OK }
            headers {
                +{ HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE }
                +{ "X-TEST-HEADER" to "TEST-VALUE" }
            }
            body { Dummy("Jun", 25) }
        }

        usedHeaderBuilder.headers().toMap() shouldBe usedUnaryPlus.headers().toMap()

    }
})
