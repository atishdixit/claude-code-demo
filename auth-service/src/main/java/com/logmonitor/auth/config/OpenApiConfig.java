package com.logmonitor.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("auth-service API")
                .description("Registers users and issues RS256-signed JWTs for the log-monitor system")
                .version("1.0.0"));
    }
}
