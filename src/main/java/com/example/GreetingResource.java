package com.example;

import java.time.Instant;
import java.util.Map;

import com.example.dto.EchoRequest;
import com.example.dto.EchoResponse;
import com.example.dto.GreetingResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * A plain JAX-RS resource. Quarkus's amazon-lambda-http extension routes
 * incoming API Gateway (HTTP API v2) / Lambda Function URL events straight
 * into this same code — no Lambda-specific handler class needed.
 */
@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingResource {

    @GET
    public GreetingResponse hello() {
        return new GreetingResponse("Hello, world!", source());
    }

    @GET
    @Path("/{name}")
    public GreetingResponse helloName(@PathParam("name") String name) {
        return new GreetingResponse("Hello, " + name + "!", source());
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    public EchoResponse echo(EchoRequest request) {
        String text = request.text() == null ? "" : request.text();
        return new EchoResponse(text, text.toUpperCase(), text.length(), Instant.now());
    }

    @GET
    @Path("/context")
    public Map<String, String> context() {
        // AWS_LAMBDA_FUNCTION_NAME etc. are only set when actually running
        // inside Lambda — this shows the same code detecting where it's running.
        String functionName = System.getenv("AWS_LAMBDA_FUNCTION_NAME");
        return Map.of(
                "runningInLambda", String.valueOf(functionName != null),
                "functionName", functionName == null ? "(local / not Lambda)" : functionName,
                "region", System.getenv().getOrDefault("AWS_REGION", "(none)")
        );
    }

    private String source() {
        boolean inLambda = System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null;
        return inLambda ? "aws-lambda" : "local";
    }
}
