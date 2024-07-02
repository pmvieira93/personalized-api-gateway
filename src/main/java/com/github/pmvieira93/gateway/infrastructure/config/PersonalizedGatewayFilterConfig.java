package com.github.pmvieira93.gateway.infrastructure.config;

import org.jboss.logging.Logger;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.pmvieira93.gateway.infrastructure.ApiVersionValidationGatewayFilterFactory;
import com.github.pmvieira93.gateway.infrastructure.LoggingRequestGatewayFilterFactory;

//import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;

@Configuration
public class PersonalizedGatewayFilterConfig {

    private static final Logger logger = Logger.getLogger(PersonalizedGatewayFilterConfig.class);
    
    static {
        logger.info("=====Loaded PersonalizedGatewayFilterConfig=====");
    }

    //@Bean("aaaaaaaaaaa")
    //public AbstractGatewayFilterFactory<?> apiVersionValidationFilterFactory() {
    //    return new ApiVersionValidationGatewayFilterFactory();
    //}

    //@Bean("LoggingFilterGatewayFilterFactory")
    //public AbstractGatewayFilterFactory<?> loggingFilterFactory() {
    //    return new LoggingFilterFactory();
    //}
}
