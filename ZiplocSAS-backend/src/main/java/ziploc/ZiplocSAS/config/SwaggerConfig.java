package ziploc.ZiplocSAS.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("ZiplocSAS – Plataforma Fintech API")
                .description("API REST para gestión de billeteras digitales con estructuras de datos personalizadas")
                .version("1.0.0")
                .contact(new Contact()
                        .name("ZiplocSAS")
                        .email("dev@ziplocSAS.com")));
    }
}