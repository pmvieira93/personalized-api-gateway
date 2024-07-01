package com.github.pmvieira93.gateway;

import org.springframework.boot.SpringApplication;

public class TestSpringGatewayFiltersApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringGatewayFiltersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
