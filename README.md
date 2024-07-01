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

```bash
# Test Route: 'rewrite_route'
curl -v 'http://localhost:8080/foo/ip' -H 'host: pedro.rewrite.org'

# Test Route: 'header_path_rewrite_route'
curl -v 'http://localhost:8080/self/ip' -H 'X-Request-Id: 123'
```

## Personalized Filters and Predicates

