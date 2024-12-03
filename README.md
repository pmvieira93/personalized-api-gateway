# Spring Cloud Gateway

## Initializer

### Docker Commands

Run Service
```bash
docker-compose up --build -d
```

Stop Service
```bash
docker-compose down
```

### Setup FGA & Environment

Initializer FGA Service
```bash
newman run fga-initializer.postman_collection.json --export-globals ./target/generated-ids.json
```

Parse IDs into `.env` file
```bash
jq -r '.values | map("export \(.key|sub("-";"_"; "g")|ascii_upcase)=\(.value|tostring)")|.[]' ./target/generated-ids.json > ./target/.spring-env
```

Load Env variables
```bash
source .spring-env
```

## Maven Commands

Run application
```bash
mvnw spring-boot:run
```

Debug mode
```bash
mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

Compile Application
```bash
mvnw clean compile
```

## Test Commands

### Unit & Integration Tests

```bash
mvnw verify
```

Running all tests
```bash
mvnw test
```

Running specific test class
```bash
mvnw -Dtest=RouteGatewayFilterIntegrationTest test
```

### Manually Tests

Testing Routes using default Predicates/Filters

```bash
# Test Route: 'code_rewrite_route'
curl -v 'http://localhost:8080/foo/ip' -H 'host: pedro.rewrite.org'

# Test Route: 'yml_header_path_rewrite_route'
curl -v 'http://localhost:8080/self/ip' -H 'X-Request-Id: 123'
```

## Personalized Filters and Predicates


### Manually Tests

Testing Routes using personalized Predicates/Filters

**ApiVersionValidation**

```bash
# Test Route: 'code_path_rewrite_apiversion_route'
#   > Expected: 200
curl -v 'http://localhost:8080/version/ip' -H 'api-version: 4.5.0'
#   > Expected: 403
curl -v 'http://localhost:8080/version/ip' -H 'api-version: 1.5.0'
```

```bash
# Test Route: 'httpbin_apiversionfilter_v1_route' (Default Header)
#   > Expected: 200
curl -v 'http://localhost:8080/v1/ip' -H 'api-version: 4.5.0'
#   > Expected: 403
curl -v 'http://localhost:8080/v1/ip' -H 'api-version: 1.5.0'

# Test Route: 'httpbin_apiversionfilter_v1_route' (Configured Header)
#   > Expected: 200
curl -v 'http://localhost:8080/v1/ip' -H 'ApiVersion: 4.5.0'
#   > Expected: 403
curl -v 'http://localhost:8080/v1/ip' -H 'ApiVersion: 1.5.0'
```

**TraceRequest**

```bash
# Test Route: 'httpbin_trace_route'
#   > Expected: 200
curl -v 'http://localhost:8080/trace/ip' -H 'dummy: trace'
```

## References

- [Creating a custom Spring Cloud Gateway Filter](https://spring.io/blog/2022/08/26/creating-a-custom-spring-cloud-gateway-filter)
- [Building a Gateway](https://spring.io/guides/gs/gateway)
- [Spring Cloud Gateway Sample](https://github.com/spring-cloud-samples/spring-cloud-gateway-sample)
- [Properties with Spring](https://www.baeldung.com/properties-with-spring)
- [Writing Custom Spring Cloud Gateway Filters](https://www.baeldung.com/spring-cloud-custom-gateway-filters)
- [gatewayapp.routes](https://github.com/eugenp/tutorials/blob/master/spring-cloud-modules/spring-cloud-gateway/src/main/java/com/baeldung/springcloudgateway/customfilters/gatewayapp/routes/ServiceRouteConfiguration.java)
- [RewritePath GatewayFilter Factory](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/gatewayfilter-factories/rewritepath-factory.html)
- [Class RewritePathGatewayFilterFactory](https://www.javadoc.io/static/org.springframework.cloud/spring-cloud-gateway-core/2.2.0.RELEASE/org/springframework/cloud/gateway/filter/factory/RewritePathGatewayFilterFactory.html)