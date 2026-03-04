package com.fiap.videoframeextractor.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VideoProcessingException - Testes")
class VideoProcessingExceptionTest {

    @Test
    @DisplayName("Deve criar exceção apenas com mensagem")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Falha no processamento do vídeo";

        VideoProcessingException exception = new VideoProcessingException(mensagem);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        String mensagem = "Falha ao processar vídeo";
        IllegalArgumentException causa = new IllegalArgumentException("Formato inválido");

        VideoProcessingException exception = new VideoProcessingException(mensagem, causa);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isEqualTo(causa);
        assertThat(exception.getCause().getMessage()).isEqualTo("Formato inválido");
    }

    @Test
    @DisplayName("Deve ser instância de RuntimeException")
    void deveSerInstanciaDeRuntimeException() {
        VideoProcessingException exception = new VideoProcessingException("erro");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve encadear múltiplas causas")
    void deveEncadearMultiplasCausas() {
        RuntimeException causaRaiz = new RuntimeException("database error");
        VideoProcessingException causaIntermediaria = new VideoProcessingException("erro intermediário", causaRaiz);
        VideoProcessingException excecaoFinal = new VideoProcessingException("erro final", causaIntermediaria);

        assertThat(excecaoFinal.getCause()).isEqualTo(causaIntermediaria);
        assertThat(excecaoFinal.getCause().getCause()).isEqualTo(causaRaiz);
    }
}
