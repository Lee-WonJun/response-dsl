# response-dsl

Kotlin DSL for ServerResponse.

- [x] servlet DSL
- [x] reative DSL

## Usage

### Get Started
Original:

```kotlin
val response = ServerResponse.ok()
    .contentType(MediaType.TEXT_PLAIN)
    .header("X-TEST-A", "1234")
    .header("X-TEST-B", "5678")
    .body("Hello, World!")
```

DSL
```kotlin
val response = response {
    status { HttpStatus.OK }
    contentType { MediaType.TEXT_PLAIN }
    headers {
        header { "X-TEST-A" to "1234" }
        header { "X-TEST-B" to "5678" }
    }
    body { "Hello, World!" }
}
```

Reative DSL
```kotlin
val responseWithBodyValue = withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
    response {
        status { HttpStatus.OK }
        contentType { MediaType.TEXT_PLAIN }
        header { "X-TEST-HEADER" to "TEST-VALUE" }
        bodyValue { "Hello World" }
    }
}

val responseWithBody =  withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
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
```


### Higher Order Function for setting default values
```kotlin
val ok = makeDefaultResponse(HttpStatus.OK)
val ok_json = makeDefaultResponse(HttpStatus.OK, MediaType.APPLICATION_JSON)
val ok_text_x_header = makeDefaultResponse(
    HttpStatus.OK, MediaType.TEXT_PLAIN, mapOf("X-TEST-A" to "1234", "X-TEST-B" to "5678"))

val response = ok_text_x_header {
    body { "Hello, World!" }
}
```
