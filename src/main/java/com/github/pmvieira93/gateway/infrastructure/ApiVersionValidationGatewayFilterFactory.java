package com.github.pmvieira93.gateway.infrastructure;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

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
        return new ApiVersionValidationFilter();
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
