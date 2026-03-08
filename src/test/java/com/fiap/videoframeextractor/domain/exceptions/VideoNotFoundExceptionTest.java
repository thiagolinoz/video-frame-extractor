package com.fiap.videoframeextractor.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VideoNotFoundException - Testes")
class VideoNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar exceção apenas com mensagem")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Vídeo não encontrado: video-123";

        VideoNotFoundException exception = new VideoNotFoundException(mensagem);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        String mensagem = "Vídeo não encontrado no S3";
        RuntimeException causa = new RuntimeException("404 Not Found");

        VideoNotFoundException exception = new VideoNotFoundException(mensagem, causa);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isEqualTo(causa);
        assertThat(exception.getCause().getMessage()).isEqualTo("404 Not Found");
    }

    @Test
    @DisplayName("Deve ser instância de RuntimeException")
    void deveSerInstanciaDeRuntimeException() {
        VideoNotFoundException exception = new VideoNotFoundException("erro");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve incluir identificador do vídeo na mensagem")
    void deveIncluirIdentificadorDoVideoNaMensagem() {
        String videoId = "abc-456";
        String mensagem = "Vídeo não encontrado no S3: " + videoId;

        VideoNotFoundException exception = new VideoNotFoundException(mensagem);

        assertThat(exception.getMessage()).contains(videoId);
    }
}
