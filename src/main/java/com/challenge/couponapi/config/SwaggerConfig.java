package com.challenge.couponapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	/**
     * Configures the OpenAPI definition for the Coupon API.
     * Provides basic metadata such as title, version, and description for Swagger UI.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Coupon API")
                .version("1.0")
                .description("API for managing coupon codes"));
    }
}