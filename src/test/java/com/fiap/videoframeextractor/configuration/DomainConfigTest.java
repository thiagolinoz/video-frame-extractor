package com.fiap.videoframeextractor.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainConfig - Testes Unitários")
class DomainConfigTest {

    @Test
    @DisplayName("Deve ser anotada com @Configuration")
    void deveSerAnotadaComConfiguration() {
        Configuration annotation = DomainConfig.class.getAnnotation(Configuration.class);

        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Deve ser anotada com @ComponentScan")
    void deveSerAnotadaComComponentScan() {
        ComponentScan annotation = DomainConfig.class.getAnnotation(ComponentScan.class);

        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Deve incluir pacote de services no ComponentScan")
    void deveIncluirPacoteDeServicesNoComponentScan() {
        ComponentScan annotation = DomainConfig.class.getAnnotation(ComponentScan.class);

        assertThat(annotation.basePackages())
                .contains("com.fiap.videoframeextractor.domain.services");
    }

    @Test
    @DisplayName("Deve incluir pacote de infrastructure no ComponentScan")
    void deveIncluirPacoteDeInfrastructureNoComponentScan() {
        ComponentScan annotation = DomainConfig.class.getAnnotation(ComponentScan.class);

        assertThat(annotation.basePackages())
                .contains("com.fiap.videoframeextractor.infrastructure");
    }

    @Test
    @DisplayName("Deve ser instanciável sem dependências externas")
    void deveSerInstanciavelSemDependencias() {
        DomainConfig config = new DomainConfig();

        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("Deve escanear exatamente dois pacotes base")
    void deveEscanearDoisPacotesBase() {
        ComponentScan annotation = DomainConfig.class.getAnnotation(ComponentScan.class);

        assertThat(annotation.basePackages()).hasSize(2);
    }
}
