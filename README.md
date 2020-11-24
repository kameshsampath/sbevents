# Spring Cloud Functions - Cloud Events

Welcome to your new Spring Cloud Function project!

This sample project contains a single function: `functions.CloudFunctionApplication.uppercase()`, which returns the uppercase of the data passed via CloudEvents

## Local execution

Make sure that `Java 11 SDK` is installed.

To start server locally run `./mvnw spring-boot:run`.
The command starts http server and automatically watches for changes of source code.
If source code changes the change will be propagated to running server. It also opens debugging port `5005`
so debugger can be attached if needed.

To run test locally run `./mvnw test`.

### cURL

```shell script
URL=http://localhost:8080/uppercase
curl -v ${URL} \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Subject:Uppercase" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:dev.kameshs.example" \
  -H "Ce-Specversion:1.0" \
  -d "{\"input\": \"$(whoami)\"}\""
```

### HTTPie

```shell script
URL=http://localhost:8080/uppercase
http -v ${URL} \
  Content-Type:application/json \
  Ce-Id:1 \
  Ce-Subject:Uppercase \
  Ce-Source:cloud-event-example \
  Ce-Type:dev.kameshs.example \
  Ce-Specversion:1.0 \
  input=$(whoami)
```
