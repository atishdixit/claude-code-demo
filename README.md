# quarkus-lambda-demo

A minimal example of a [Quarkus](https://quarkus.io) REST API deployed as a single
AWS Lambda function — same JAX-RS code runs locally (`quarkus dev`) and in Lambda
behind an API Gateway HTTP API or a Lambda Function URL, no separate handler class
needed.

## How it works

Quarkus's `amazon-lambda-http` extension replaces the normal HTTP entry point with
one that understands API Gateway (HTTP API v2) and Function URL event payloads,
translates them into regular HTTP requests, routes them through the same JAX-RS
resources you'd use in any Quarkus web app, and translates the response back into
the Lambda-expected JSON shape (`statusCode`, `headers`, `body`). [`GreetingResource`](src/main/java/com/example/GreetingResource.java)
is plain JAX-RS — nothing Lambda-specific in the business code.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/hello` | Returns a greeting |
| GET | `/hello/{name}` | Greets `{name}` |
| POST | `/hello/echo` | Body `{"text": "..."}` → echoes it back uppercased, with length |
| GET | `/hello/context` | Reports whether the code is actually running inside Lambda (reads `AWS_LAMBDA_FUNCTION_NAME`) |

## Run it locally

```bash
mvn quarkus:dev
```

Dev mode starts a mock Lambda event server, so the code runs through the *real*
Lambda code path even locally — not a plain fallback HTTP server. Then either:

- Hit the endpoints directly, e.g. `curl http://localhost:8080/hello` — Quarkus
  routes these through the mock Lambda translation layer transparently.
- Or send a raw, AWS-shaped event JSON straight at the mock Lambda endpoint, exactly
  like the real Lambda Runtime API would deliver it:

  ```bash
  curl -s -X POST http://localhost:8080/_lambda_ \
    -H "Content-Type: application/json" \
    --data-binary @sample-events/api-gateway-v2-get-hello.json

  curl -s -X POST http://localhost:8080/_lambda_ \
    -H "Content-Type: application/json" \
    --data-binary @sample-events/api-gateway-v2-post-echo.json
  ```

  Both return the same JSON shape Lambda itself would send back to API Gateway:
  `{"statusCode": 200, "headers": {...}, "body": "..."}`.

> **Known quirk:** on some setups, `mvn quarkus:dev`'s live-reload/isolated-build
> step races the amazon-lambda-http poll loop and fails on first start with
> `IllegalArgumentException: Key already registered quarkus.http.port`. If you hit
> this, press `space` to trigger a restart in the running dev-mode terminal (it
> usually starts clean on retry), or just rely on `mvn test` below — it exercises
> the identical Lambda code path deterministically every time and is what this repo
> was actually verified with.

## Tests

```bash
mvn test
```

[`GreetingResourceTest`](src/test/java/com/example/GreetingResourceTest.java) uses
`@QuarkusTest` + RestAssured against `/hello/**`, plus two tests
(`rawApiGatewayEventGet`, `rawApiGatewayEventPostEcho`) that POST the actual JSON
files from `sample-events/` to `/_lambda_` and assert on the real Lambda response
shape — proving raw AWS event payloads are handled correctly, not just the
convenience routes. All 6 tests exercise the real amazon-lambda-http code path
(you'll see `Mock Lambda Event Server Started` in the test output), not a generic
test server.

## Building for deployment

**JVM build** (fast build, ~1–2s cold start after JIT warms up):

```bash
mvn package
```

Produces `target/function.zip`, ready to upload as a Lambda deployment package
(`Runtime: java21`).

**Native build** (slower build, near-instant cold start — usually worth it for
Lambda specifically):

```bash
mvn package -Dnative -Dquarkus.native.container-build=true
```

The `-Dquarkus.native.container-build=true` flag builds inside a container so you
don't need GraalVM installed locally (just Docker). Produces a native
`function.zip` targeting `Runtime: provided.al2023`.

## Deploying (reference only — not run in this session)

[`template.yaml`](template.yaml) is an [AWS SAM](https://docs.aws.amazon.com/serverless-application-model/)
template: one Lambda function exposed via a Function URL (no API Gateway needed for
this simple demo). Deploying requires an AWS account and credentials, which this
environment doesn't have — but for reference, once you have the AWS CLI/SAM CLI and
credentials configured:

```bash
mvn package
sam deploy --guided
```

`sam deploy --guided` will print the Function URL once deployed — that's your live
endpoint, e.g. `https://<id>.lambda-url.<region>.on.aws/hello`.

## Notes

- `quarkus-amazon-lambda-http` targets API Gateway **HTTP API (v2)** and Function
  URL payloads — the modern, recommended option. There's a separate
  `quarkus-amazon-lambda-rest` extension for the older API Gateway REST API (v1)
  payload format, and a plain `quarkus-amazon-lambda` extension for classic
  `RequestHandler<Input, Output>`-style functions with no HTTP involved at all.
- `GET /hello/context` is there specifically to make the Lambda-vs-local difference
  visible — same deployed code, different runtime environment.
