package com.github.pmvieira93.gateway.infrastructure.filter.factory;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.github.pmvieira93.gateway.infrastructure.filter.ApiVersionValidationFilterV2;

@Component
public class ApiVersionValidationV2GatewayFilterFactory extends
        AbstractGatewayFilterFactory<ApiVersionValidationV2GatewayFilterFactory.Config> {

    public ApiVersionValidationV2GatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("headerName", "returnStatus");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new ApiVersionValidationFilterV2(config);
    }

    @Getter
    @Setter
    public static class Config {

        String headerName;
        Integer returnStatus;

        public Config(String headerName, Integer returnStatus) {
            this.headerName = headerName;
            this.returnStatus = returnStatus;
        }
    }

}
