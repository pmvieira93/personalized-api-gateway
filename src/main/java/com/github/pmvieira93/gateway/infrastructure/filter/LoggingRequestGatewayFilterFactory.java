package com.github.pmvieira93.gateway.infrastructure.filter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class LoggingRequestGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger logger = Logger.getLogger(LoggingRequestGatewayFilterFactory.class);

    static {
        logger.info("=====Loaded LoggingFilterFactory=====");
    }

    /*
     * baseMessage: "Request to example route"
     * details: true
     */

    /**
     * BaseMessage key. String
     */
    public static final String BASE_MESSAGE_KEY = "BASE_MESSAGE_KEY";

    /**
     * Details key. Boolean
     */
    public static final String DETAILS_KEY = "details";

    public LoggingRequestGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(BASE_MESSAGE_KEY, DETAILS_KEY);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String requestId = exchange.getRequest().getId();
            Long requestStarts = Instant.now().toEpochMilli();

            logger.info("Pre-filter: request id ->" + requestId);
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                exchange.getResponse().getHeaders().add("X-Request-Id", requestId);
                exchange.getResponse().getHeaders().add("X-Request-Duration",
                        (Instant.now().toEpochMilli() - requestStarts) + "ms");
                logger.info("Post-filter: response code -> " + exchange.getResponse().getStatusCode());
            }));
        };
    }
}
