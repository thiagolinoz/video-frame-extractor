package com.fiap.videoframeextractor.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StorageException - Testes")
class StorageExceptionTest {

    @Test
    @DisplayName("Deve criar exceção apenas com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Given que existe uma mensagem de erro "Erro no armazenamento S3"
        String mensagem = "Erro no armazenamento S3";
       // When - eu criar uma StorageException usando apenas essa mensagem
        StorageException exception = new StorageException(mensagem);
       // Then - a exceção deve conter a mensagem "Erro no armazenamento S3"
        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        String mensagem = "Falha ao fazer upload";
        RuntimeException causa = new RuntimeException("Conexão recusada");

        StorageException exception = new StorageException(mensagem, causa);

        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isEqualTo(causa);
        assertThat(exception.getCause().getMessage()).isEqualTo("Conexão recusada");
    }

    @Test
    @DisplayName("Deve ser instância de RuntimeException")
    void deveSerInstanciaDeRuntimeException() {
        StorageException exception = new StorageException("erro");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve preservar tipo da causa")
    void devePreservarTipoDaCausa() {
        java.io.IOException causa = new java.io.IOException("falha I/O");

        StorageException exception = new StorageException("erro storage", causa);

        assertThat(exception.getCause()).isInstanceOf(java.io.IOException.class);
    }
}
