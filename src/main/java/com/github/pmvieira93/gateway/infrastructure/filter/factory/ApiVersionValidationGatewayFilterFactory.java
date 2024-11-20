package com.github.pmvieira93.gateway.infrastructure.filter.factory;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.github.pmvieira93.gateway.infrastructure.filter.ApiVersionValidationFilter;

@Slf4j
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

    @Getter
    @Setter
    public static class Config {

        String headerName;

        public Config(String headerName) {
            this.headerName = headerName;
        }

    }

}
