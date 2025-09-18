package com.zl.mjga.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway")
                        .version("1.0")
                        .description("API Gateway 接口文档")
                        .contact(new Contact()
                                .name("roc")
                                .url("https://github.com/roc80")
                                .email("lipeng1080@gmail.com")
                        )
                );
    }

}
