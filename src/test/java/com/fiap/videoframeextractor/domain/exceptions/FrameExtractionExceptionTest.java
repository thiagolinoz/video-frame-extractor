package com.fiap.videoframeextractor.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FrameExtractionException - Testes")
class FrameExtractionExceptionTest {

    @Test
    @DisplayName("Deve criar exceção apenas com mensagem")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Erro na extração de frames";

        FrameExtractionException exception = new FrameExtractionException(mensagem);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        String mensagem = "Erro na extração de frames";
        RuntimeException causa = new RuntimeException("Causa raiz");

        FrameExtractionException exception = new FrameExtractionException(mensagem, causa);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isEqualTo(causa);
        assertThat(exception.getCause().getMessage()).isEqualTo("Causa raiz");
    }

    @Test
    @DisplayName("Deve ser instância de RuntimeException")
    void deveSerInstanciaDeRuntimeException() {
        FrameExtractionException exception = new FrameExtractionException("erro");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve preservar stack trace da causa")
    void devePreservarStackTraceDaCausa() {
        IllegalStateException causa = new IllegalStateException("estado inválido");

        FrameExtractionException exception = new FrameExtractionException("erro extração", causa);

        assertThat(exception.getCause()).isInstanceOf(IllegalStateException.class);
    }
}
