package com.github.pmvieira93.gateway.infrastructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ApiVersionValidationFilter implements GatewayFilter {

    static final String VERSION_REGEX = "([0-9]+\\.[0-9]+\\.[0-9]+)";

    static final String DEFAULT_HEADER = "api-version";

    static final Logger logger = Logger.getLogger(ApiVersionValidationFilter.class);

    ApiVersionValidationGatewayFilterFactory.Config args;

    private static final Double versionGreaterThan = 3.11D;

    public ApiVersionValidationFilter() {
    }

    // public ApiVersionValidationFilter(ApiVersionValidationFilterFactory.Config
    // args) {
    // this.args = args;
    // }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String headerValue = exchange.getRequest().getHeaders().getFirst(DEFAULT_HEADER);
        logger.debug("Header value: " + headerValue);
        Mono<Boolean> result = validateApiVersion(headerValue);
        return result.flatMap(isValid -> {
            if (isValid) {
                return chain.filter(exchange);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        });
    }

    private Mono<Boolean> validateApiVersion(String version) {
        Double requestedVersion = versionStringToDouble(version);
        logger.info("Requested version: " + requestedVersion + " | Version greater than: " + versionGreaterThan);
        return Mono.just(requestedVersion >= versionGreaterThan);
    }

    private Double versionStringToDouble(final String version) {
        final Pattern pattern = Pattern.compile(VERSION_REGEX);
        final Matcher matcher = pattern.matcher(version);
        Double result = 0.0D;
        if (matcher.find()) {
            logger.debug("Version found: " + matcher.group(1));
            String numericString = matcher.group(1);
            final int endIndex = numericString.lastIndexOf(".");
            final StringBuffer buffer = new StringBuffer(numericString);
            numericString = buffer.replace(endIndex, endIndex + 1, "").toString();
            result = Double.parseDouble(numericString);
        }
        return result;
    }

}
