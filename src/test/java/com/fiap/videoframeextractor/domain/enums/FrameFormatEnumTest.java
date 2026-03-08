package com.fiap.videoframeextractor.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FrameFormatEnum - Testes")
class FrameFormatEnumTest {

    @Test
    @DisplayName("Deve retornar formato 'jpeg' para JPEG")
    void deveRetornarFormatoJpeg() {
        assertThat(FrameFormatEnum.JPEG.getFormat()).isEqualTo("jpeg");
    }

    @Test
    @DisplayName("Deve retornar formato 'png' para PNG")
    void deveRetornarFormatoPng() {
        assertThat(FrameFormatEnum.PNG.getFormat()).isEqualTo("png");
    }

    @Test
    @DisplayName("Deve retornar formato 'bmp' para BMP")
    void deveRetornarFormatoBmp() {
        assertThat(FrameFormatEnum.BMP.getFormat()).isEqualTo("bmp");
    }

    @Test
    @DisplayName("Deve conter exatamente 3 valores")
    void deveConterTresValores() {
        assertThat(FrameFormatEnum.values()).hasSize(3);
    }

    @Test
    @DisplayName("Deve retornar enum correto pelo nome via valueOf")
    void deveRetornarEnumPorNome() {
        assertThat(FrameFormatEnum.valueOf("JPEG")).isEqualTo(FrameFormatEnum.JPEG);
        assertThat(FrameFormatEnum.valueOf("PNG")).isEqualTo(FrameFormatEnum.PNG);
        assertThat(FrameFormatEnum.valueOf("BMP")).isEqualTo(FrameFormatEnum.BMP);
    }

    @Test
    @DisplayName("Deve lançar exceção para nome inválido")
    void deveLancarExcecaoParaNomeInvalido() {
        assertThatThrownBy(() -> FrameFormatEnum.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve retornar nome correto do enum via name()")
    void deveRetornarNomeCorreto() {
        assertThat(FrameFormatEnum.JPEG.name()).isEqualTo("JPEG");
        assertThat(FrameFormatEnum.PNG.name()).isEqualTo("PNG");
        assertThat(FrameFormatEnum.BMP.name()).isEqualTo("BMP");
    }

    @Test
    @DisplayName("Deve retornar ordinal correto de cada valor")
    void deveRetornarOrdinalCorreto() {
        assertThat(FrameFormatEnum.JPEG.ordinal()).isZero();
        assertThat(FrameFormatEnum.PNG.ordinal()).isEqualTo(1);
        assertThat(FrameFormatEnum.BMP.ordinal()).isEqualTo(2);
    }
}
