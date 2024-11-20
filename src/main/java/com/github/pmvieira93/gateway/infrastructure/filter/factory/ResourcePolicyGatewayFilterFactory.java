package com.github.pmvieira93.gateway.infrastructure.filter.factory;

import com.github.pmvieira93.gateway.infrastructure.filter.ResourcePolicyFilter;
import com.github.pmvieira93.gateway.infrastructure.security.JwtTokenProvider;
import dev.openfga.sdk.api.client.OpenFgaClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ResourcePolicyGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ResourcePolicyGatewayFilterFactory.Config> {

    static {
        log.info("=====Loaded ResourcePolicyGatewayFilterFactory=====");
    }

    private final OpenFgaClient fgaClient;
    private final JwtTokenProvider jwtTokenProvider;

    public ResourcePolicyGatewayFilterFactory(final OpenFgaClient fgaClient, final JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.fgaClient = fgaClient;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new ResourcePolicyFilter(config, fgaClient, jwtTokenProvider);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("openfgaStoreId", "openFgaAuthModelId");
    }

    /**
     * Definition of Required configs to use this Filter
     */
    @Getter
    public static class Config {

        String openfgaStoreId;
        String openFgaAuthModelId;

        public Config(String openfgaStoreId) {
            this.openfgaStoreId = openfgaStoreId;
        }

        public Config(String openfgaStoreId, String openFgaAuthModelId) {
            this.openfgaStoreId = openfgaStoreId;
            this.openFgaAuthModelId = openFgaAuthModelId;
        }
    }
}
