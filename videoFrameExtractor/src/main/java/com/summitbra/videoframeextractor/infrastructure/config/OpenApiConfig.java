package com.summitbra.videoframeextractor.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
            .url("http://localhost:" + serverPort)
            .description("Servidor de desenvolvimento");

        Server dockerServer = new Server()
            .url("http://localhost:" + serverPort)
            .description("Servidor Docker");

        return new OpenAPI()
            .info(new Info()
                .title("Video Frame Extractor API")
                .description("API REST para extração de frames de vídeos usando FFmpeg")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Summit BRA")
                    .email("contato@summitbra.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(localServer, dockerServer));
    }
}
