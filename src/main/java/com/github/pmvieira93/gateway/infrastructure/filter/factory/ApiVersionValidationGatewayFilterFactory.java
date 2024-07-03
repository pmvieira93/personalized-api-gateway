package com.github.pmvieira93.gateway.infrastructure.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.github.pmvieira93.gateway.infrastructure.filter.ApiVersionValidationFilter;

@Component
public class ApiVersionValidationGatewayFilterFactory extends
        AbstractGatewayFilterFactory<ApiVersionValidationGatewayFilterFactory.Config> {

    public ApiVersionValidationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("headerName");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new ApiVersionValidationFilter(config);
    }

    public static class Config {

        String headerName;

        public Config() {
        }

        public Config(String headerName) {
            this.headerName = headerName;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

    }

}
