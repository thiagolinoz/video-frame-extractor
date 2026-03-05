package com.fiap.videoframeextractor.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VideoStatusEnum - Testes")
class VideoStatusEnumTest {

    @Test
    @DisplayName("Deve conter exatamente 4 status")
    void deveConterQuatroStatus() {
        assertThat(VideoStatusEnum.values()).hasSize(4);
    }

    @Test
    @DisplayName("Deve conter status RECEIVED")
    void deveConterStatusReceived() {
        assertThat(VideoStatusEnum.valueOf("RECEIVED")).isEqualTo(VideoStatusEnum.RECEIVED);
    }

    @Test
    @DisplayName("Deve conter status PROCESSING")
    void deveConterStatusProcessing() {
        assertThat(VideoStatusEnum.valueOf("PROCESSING")).isEqualTo(VideoStatusEnum.PROCESSING);
    }

    @Test
    @DisplayName("Deve conter status COMPLETED")
    void deveConterStatusCompleted() {
        assertThat(VideoStatusEnum.valueOf("COMPLETED")).isEqualTo(VideoStatusEnum.COMPLETED);
    }

    @Test
    @DisplayName("Deve conter status PROCESS_ERROR")
    void deveConterStatusProcessError() {
        assertThat(VideoStatusEnum.valueOf("PROCESS_ERROR")).isEqualTo(VideoStatusEnum.PROCESS_ERROR);
    }

    @Test
    @DisplayName("Deve lançar exceção para status inválido")
    void deveLancarExcecaoParaStatusInvalido() {
        assertThatThrownBy(() -> VideoStatusEnum.valueOf("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve retornar ordinais corretos")
    void deveRetornarOrdinaisCorretos() {
        assertThat(VideoStatusEnum.RECEIVED.ordinal()).isZero();
        assertThat(VideoStatusEnum.PROCESSING.ordinal()).isEqualTo(1);
        assertThat(VideoStatusEnum.COMPLETED.ordinal()).isEqualTo(2);
        assertThat(VideoStatusEnum.PROCESS_ERROR.ordinal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve retornar nome correto via name()")
    void deveRetornarNomeCorreto() {
        assertThat(VideoStatusEnum.RECEIVED.name()).isEqualTo("RECEIVED");
        assertThat(VideoStatusEnum.PROCESSING.name()).isEqualTo("PROCESSING");
        assertThat(VideoStatusEnum.COMPLETED.name()).isEqualTo("COMPLETED");
        assertThat(VideoStatusEnum.PROCESS_ERROR.name()).isEqualTo("PROCESS_ERROR");
    }
}
