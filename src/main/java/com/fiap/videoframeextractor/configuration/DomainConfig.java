package com.fiap.videoframeextractor.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.fiap.videoframeextractor.domain.services",
    "com.fiap.videoframeextractor.infrastructure"
})
public class DomainConfig {
}
