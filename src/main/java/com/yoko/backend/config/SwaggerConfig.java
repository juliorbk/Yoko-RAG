package com.yoko.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().info(
      new Info()
        .title("Yoko RAG API")
        .version("1.0.0")
        .description(
          "API REST para el asistente virtual de la UNEG, impulsado por Spring AI y PostgreSQL."
        )
        .contact(
          new Contact().name("Julio Suarez").email("jsuarez@uneg.edu.ve")
        )
    );
  }
}
