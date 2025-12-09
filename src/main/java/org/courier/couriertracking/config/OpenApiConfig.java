package org.courier.couriertracking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI courierTrackingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Courier Tracking API")
                        .description("Courier location tracking, distance calculation and Migros store entrances by Emre Aydın")
                        .version("v1.0.0"));
    }
}
