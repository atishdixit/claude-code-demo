package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GreetingResourceTest {

    @Test
    void helloDefault() {
        given()
            .when().get("/hello")
            .then()
                .statusCode(200)
                .body("message", equalTo("Hello, world!"));
    }

    @Test
    void helloName() {
        given()
            .when().get("/hello/Alice")
            .then()
                .statusCode(200)
                .body("message", equalTo("Hello, Alice!"));
    }

    @Test
    void echo() {
        given()
            .contentType("application/json")
            .body("{\"text\":\"abc\"}")
            .when().post("/hello/echo")
            .then()
                .statusCode(200)
                .body("original", equalTo("abc"))
                .body("upper", equalTo("ABC"))
                .body("length", is(3));
    }

    @Test
    void context() {
        given()
            .when().get("/hello/context")
            .then()
                .statusCode(200)
                .body("runningInLambda", equalTo("false"));
    }

    /**
     * Proves the whole point of this demo: a raw, AWS-shaped API Gateway (HTTP
     * API v2) event — exactly what Lambda's Runtime API would deliver — gets
     * routed through the real amazon-lambda-http code path and comes back as a
     * proper Lambda response ({"statusCode":200, "body": "...", ...}).
     */
    @Test
    void rawApiGatewayEventGet() throws Exception {
        String event = Files.readString(Path.of("sample-events/api-gateway-v2-get-hello.json"));

        given()
            .contentType("application/json")
            .body(event)
            .when().post("/_lambda_")
            .then()
                .statusCode(200)
                .body("statusCode", is(200))
                .body("body", equalTo("{\"message\":\"Hello, world!\",\"source\":\"local\"}"));
    }

    @Test
    void rawApiGatewayEventPostEcho() throws Exception {
        String event = Files.readString(Path.of("sample-events/api-gateway-v2-post-echo.json"));

        given()
            .contentType("application/json")
            .body(event)
            .when().post("/_lambda_")
            .then()
                .statusCode(200)
                .body("statusCode", is(200))
                .body("body", org.hamcrest.Matchers.containsString("HELLO FROM API GATEWAY"));
    }
}
