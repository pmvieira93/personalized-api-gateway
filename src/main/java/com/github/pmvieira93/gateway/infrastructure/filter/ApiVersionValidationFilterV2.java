package com.github.pmvieira93.gateway.infrastructure.filter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.github.pmvieira93.gateway.infrastructure.filter.factory.ApiVersionValidationV2GatewayFilterFactory;

import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ApiVersionValidationFilterV2 implements GatewayFilter {

    static final String VERSION_REGEX = "([0-9]+\\.[0-9]+\\.[0-9]+)";
    static final String DEFAULT_HEADER = "api-version";
    static final Double versionGreaterThan = 3.11D;

    ApiVersionValidationV2GatewayFilterFactory.Config args;

    public ApiVersionValidationFilterV2() {
    }

    public ApiVersionValidationFilterV2(ApiVersionValidationV2GatewayFilterFactory.Config args) {
        this.args = args;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String headerKey = exchange.getRequest().getHeaders().containsKey(args.getHeaderName()) ? args.getHeaderName()
                : DEFAULT_HEADER;
        String headerValue = exchange.getRequest().getHeaders().getFirst(headerKey);
        log.debug("Header key|value: " + headerKey + "|" + headerValue);
        if (Objects.isNull(headerValue)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Mono<Boolean> result = validateApiVersion(headerValue);
        return result.flatMap(isValid -> {
            if (isValid) {
                return chain.filter(exchange);
            } else {
                return Mono.error(new ResponseStatusException(HttpStatus.valueOf(this.args.getReturnStatus())));
            }
        });
    }

    private Mono<Boolean> validateApiVersion(String version) {
        Double requestedVersion = versionStringToDouble(version);
        log.info("Requested version: " + requestedVersion + " | Version greater than: " + versionGreaterThan);
        return Mono.just(requestedVersion >= versionGreaterThan);
    }

    private Double versionStringToDouble(final String version) {
        final Pattern pattern = Pattern.compile(VERSION_REGEX);
        final Matcher matcher = pattern.matcher(version);
        Double result = 0.0D;
        if (matcher.find()) {
            log.debug("Version found: " + matcher.group(1));
            String numericString = matcher.group(1);
            final int endIndex = numericString.lastIndexOf(".");
            final StringBuffer buffer = new StringBuffer(numericString);
            numericString = buffer.replace(endIndex, endIndex + 1, "").toString();
            result = Double.parseDouble(numericString);
        }
        return result;
    }

}
