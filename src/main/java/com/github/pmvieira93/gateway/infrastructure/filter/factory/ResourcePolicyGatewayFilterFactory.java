package com.github.pmvieira93.gateway.infrastructure.filter.factory;

import com.github.pmvieira93.gateway.infrastructure.filter.ResourcePolicyFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ResourcePolicyGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ResourcePolicyGatewayFilterFactory.Config> {

    public ResourcePolicyGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new ResourcePolicyFilter(config);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList();
    }

    /**
     * Definition of Required configs to use this Filter
     */
    public static class Config {

        String openfgaUri;
        String openfgaToken;
        String openfgaStoreId;

        public String getOpenfgaUri() {
            return openfgaUri;
        }

        public String getOpenfgaToken() {
            return openfgaToken;
        }

        public String getOpenfgaStoreId() {
            return openfgaStoreId;
        }
    }
}
