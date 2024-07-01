# Spring Cloud Gateway

## Maven Commands

Run application

`mvnw spring-boot:run`

Compile Application

`mvnw clean compile`


## Test Commands

### Unit & Integration Tests

`mvnw verify`

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

**ApiVersionValidationFilter**

```bash
# Test Route: 'code_path_rewrite_apiversion_route'
#   > Expected: 200
curl -v 'http://localhost:8080/version/ip' -H 'api-version: 4.5.0'
#   > Expected: 403
curl -v 'http://localhost:8080/version/ip' -H 'api-version: 1.5.0'
```
