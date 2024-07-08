package com.github.pmvieira93.gateway.infrastructure.filter.factory;

import java.util.Arrays;
import java.util.List;

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

    public static class Config {

        String headerName;

        Integer returnStatus;

        public Config() {
        }

        public Config(String headerName, Integer returnStatus) {
            this.headerName = headerName;
            this.returnStatus = returnStatus;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public Integer getReturnStatus() {
            return returnStatus;
        }

        public void setReturnStatus(Integer returnStatus) {
            this.returnStatus = returnStatus;
        }
    }

}
