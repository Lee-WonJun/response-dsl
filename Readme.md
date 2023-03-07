# response-dsl

Kotlin DSL for ServerResponse.

- [x] servlet DSL
- [ ] webflux DSL

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
    status = HttpStatus.OK
    contentType = MediaType.TEXT_PLAIN
    headers {
        "X-TEST-A" to "1234"
        "X-TEST-B" to "5678"
    }
    body { "Hello, World!" }
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
