package com.github.pmvieira93.gateway.infrastructure.filter;

import com.github.pmvieira93.gateway.infrastructure.filter.factory.ResourcePolicyGatewayFilterFactory;
import com.github.pmvieira93.gateway.infrastructure.security.JwtTokenProvider;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.configuration.ApiToken;
import dev.openfga.sdk.api.configuration.ClientCheckOptions;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.Credentials;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Component
public class ResourcePolicyFilter implements GatewayFilter {

    static final String USER_PREFIX = "user:";
    static final String OBJECT_PREFIX = "resource:";

    static final Logger logger = Logger.getLogger(ResourcePolicyFilter.class);

    ResourcePolicyGatewayFilterFactory.Config args;
    OpenFgaClient client;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public ResourcePolicyFilter() {
    }

    public ResourcePolicyFilter(ResourcePolicyGatewayFilterFactory.Config args) {
        this.args = args;
        this.initOpenFgaClient();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final Mono<Boolean> result = isRequesterAuthorized(exchange.getRequest());
        return result.flatMap(isValid -> {
            if (isValid) {
                return chain.filter(exchange);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        });
    }

    private Mono<Boolean> isRequesterAuthorized(final ServerHttpRequest request) {
        boolean result = false;
        final String token = Objects.requireNonNullElse(request.getHeaders()
                .getFirst("Authorization"),"").substring(7).trim();
        final String userId = getUserId(token);
        if(Objects.nonNull(userId)) {
            final String httpMethod = request.getMethod().name();
            final String uri = request.getURI().toString();

            var openFgaRequest = new ClientCheckRequest()
                    .user(USER_PREFIX + userId)
                    .relation(httpMethod.toLowerCase())
                    ._object(OBJECT_PREFIX + uri);
            var openFgaOptions = new ClientCheckOptions().authorizationModelId("");
            try {
                var openFgaResponse = client.check(openFgaRequest, openFgaOptions).get();
                result = Boolean.TRUE.equals(openFgaResponse.getAllowed());
            } catch (FgaInvalidParameterException | ExecutionException | InterruptedException e) {
                logger.error(e);
            }
        }
        return Mono.just(result);
    }

    private String getUserId(final String token){
        return tokenProvider.getUser(token);
    }

    private void initOpenFgaClient() {
        var config = new ClientConfiguration().apiUrl(args.getOpenfgaUri())
                .storeId(args.getOpenfgaStoreId())
                .credentials(new Credentials(
                        new ApiToken(args.getOpenfgaToken())
                ));
        try {
            client = new OpenFgaClient(config);
        } catch (final FgaInvalidParameterException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
}