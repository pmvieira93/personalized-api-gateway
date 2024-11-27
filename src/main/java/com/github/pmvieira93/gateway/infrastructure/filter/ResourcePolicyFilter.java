package com.github.pmvieira93.gateway.infrastructure.filter;

import com.github.pmvieira93.gateway.infrastructure.filter.factory.ResourcePolicyGatewayFilterFactory;
import com.github.pmvieira93.gateway.infrastructure.security.JwtTokenProvider;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.configuration.ClientCheckOptions;
import dev.openfga.sdk.api.model.AuthorizationModel;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class ResourcePolicyFilter implements GatewayFilter {

    static final String USER_PREFIX = "user:";
    static final String OBJECT_PREFIX = "resource:";

    ResourcePolicyGatewayFilterFactory.Config args;
    OpenFgaClient client;
    String authModelId;
    JwtTokenProvider tokenProvider;

    public ResourcePolicyFilter() {
    }

    public ResourcePolicyFilter(ResourcePolicyGatewayFilterFactory.Config args,
                                OpenFgaClient client,
                                JwtTokenProvider tokenProvider) {
        this.args = args;
        this.client = client;
        this.tokenProvider = tokenProvider;
        this.client.setStoreId(this.args.getOpenfgaStoreId());
        this.loadAuthorizationModels();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final Mono<Boolean> result = isRequesterAuthorized(exchange.getRequest());
        return result.flatMap(isValid -> {
            if (isValid) {
                log.info("Resource policy verification successful");
                return chain.filter(exchange);
            } else {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
            }
        });
    }

    private Mono<Boolean> isRequesterAuthorized(final ServerHttpRequest request) {
        boolean result = false;
        final String token = Objects.requireNonNullElse(request.getHeaders()
                .getFirst("Authorization"), "").substring(7).trim();
        final String userId = tokenProvider.getUser(token);
        if (Objects.nonNull(userId)) {
            final String httpMethod = request.getMethod().name().toLowerCase();
            final String uri = request.getPath().value().toLowerCase();

            var openFgaRequest = new ClientCheckRequest()
                    .user(USER_PREFIX + userId)
                    .relation(httpMethod)
                    ._object(OBJECT_PREFIX + uri);
            log.info("Checking authorization model {} {} {}", userId, httpMethod, uri);
            var openFgaOptions = new ClientCheckOptions().authorizationModelId(this.authModelId);
            try {
                var openFgaResponse = client.check(openFgaRequest, openFgaOptions).get();
                result = Boolean.TRUE.equals(openFgaResponse.getAllowed());
            } catch (FgaInvalidParameterException | InterruptedException | ExecutionException e) {
                log.error("Fail call openFGA: {0}",e);
            }
        }
        return Mono.just(result);
    }

    private void loadAuthorizationModels() {
        try {
            var response = this.client.readAuthorizationModels().get();
            if (Objects.nonNull(response)) {
                AuthorizationModel higher = response.getAuthorizationModels().stream()
                        .reduce((auth1, auth2) -> auth1.getTypeDefinitions().size() > auth2.getTypeDefinitions().size()?
                                auth1 : auth2)
                        .orElse(null);
                if (Objects.nonNull(higher)) {
                    this.authModelId = higher.getId();
                    log.info("Loaded authorization model '{}'", this.authModelId);
                    this.client.setAuthorizationModelId(this.authModelId);
                }
            }
        } catch (ExecutionException | InterruptedException | FgaInvalidParameterException e) {
            log.error("Fail to load Authorization models from openFGA store: {0}",e);
            //throw new RuntimeException("Fail to load Authorization models from openFGA store",e);
        }
    }
}