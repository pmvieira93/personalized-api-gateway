package com.github.pmvieira93.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import com.github.pmvieira93.gateway.infrastructure.ApiVersionValidationFilterFactory;
import com.github.pmvieira93.gateway.infrastructure.ApiVersionValidationFilterFactory.Config;

//@ComponentScan(basePackages = "com.github.pmvieira93.infrastructure")
@SpringBootApplication
public class SpringGatewayFiltersApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringGatewayFiltersApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
			ApiVersionValidationFilterFactory apiVersionValidationFilterFactory) {
		return builder.routes()
				.route("code_path_route", r -> r.path("/get")
						.uri("http://httpbin.org"))
				.route("code_host_route", r -> r.host("*.myhost.org")
						.uri("http://httpbin.org"))
				.route("code_rewrite_route", r -> r.host("*.rewrite.org")
						.filters(f -> f.rewritePath("/foo/(?<segment>.*)", "/${segment}"))
						.uri("http://httpbin.org"))
				/*
				 * .route("hystrix_route", r -> r.host("*.hystrix.org")
				 * .filters(f -> f.hystrix(c -> c.setName("slowcmd")))
				 * .uri("http://httpbin.org"))
				 * .route("hystrix_fallback_route", r -> r.host("*.hystrixfallback.org")
				 * .filters(f -> f.hystrix(c ->
				 * c.setName("slowcmd").setFallbackUri("forward:/hystrixfallback")))
				 * .uri("http://httpbin.org"))
				 * .route("limit_route", r -> r
				 * .host("*.limited.org").and().path("/anything/**")
				 * .filters(f -> f.requestRateLimiter(c ->
				 * c.setRateLimiter(redisRateLimiter())))
				 * .uri("http://httpbin.org"))
				 */
				.route("code_path_rewrite_apiversion_route",
						r -> r.path("/version/**")
								.filters(f -> f.rewritePath("/version/?(?<segment>.*)", "/${segment}")
										.filter(apiVersionValidationFilterFactory.apply(new Config("ApiVersion"))))
								.uri("http://httpbin.org"))
				.build();
	}

}
