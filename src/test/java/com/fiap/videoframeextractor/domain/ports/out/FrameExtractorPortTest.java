package com.fiap.videoframeextractor.domain.ports.out;

import com.fiap.videoframeextractor.domain.exceptions.FrameExtractionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FrameExtractorPort - Testes de Contrato")
class FrameExtractorPortTest {

    // Implementação fake para validar o contrato da interface
    static class FakeFrameExtractor implements FrameExtractorPort {

        @Override
        public byte[] extractFramesToZip(byte[] videoData, String fileName, double interval, int maxFrames) {
            if (videoData == null || videoData.length == 0) {
                throw new FrameExtractionException("Dados do vídeo não podem ser vazios");
            }
            if (fileName == null || fileName.isBlank()) {
                throw new FrameExtractionException("Nome do arquivo não pode ser nulo ou vazio");
            }
            if (interval <= 0) {
                throw new FrameExtractionException("Intervalo deve ser maior que zero");
            }
            if (maxFrames <= 0) {
                throw new FrameExtractionException("Número máximo de frames deve ser maior que zero");
            }
            // Retorna um ZIP simulado de 100 bytes
            return new byte[100];
        }
    }

    @Test
    @DisplayName("Deve extrair frames e retornar bytes do ZIP")
    void deveExtrairFramesERetornarZip() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();
        byte[] videoData = new byte[]{1, 2, 3, 4, 5};

        byte[] resultado = extractor.extractFramesToZip(videoData, "video.mp4", 1.0, 100);

        assertThat(resultado).isNotNull().isNotEmpty();
        assertThat(resultado).hasSize(100);
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber videoData nulo")
    void deveLancarExcecaoComVideoDataNulo() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();

        assertThatThrownBy(() -> extractor.extractFramesToZip(null, "video.mp4", 1.0, 100))
                .isInstanceOf(FrameExtractionException.class)
                .hasMessageContaining("Dados do vídeo não podem ser vazios");
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber videoData vazio")
    void deveLancarExcecaoComVideoDataVazio() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();

        assertThatThrownBy(() -> extractor.extractFramesToZip(new byte[0], "video.mp4", 1.0, 100))
                .isInstanceOf(FrameExtractionException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber nome de arquivo nulo")
    void deveLancarExcecaoComNomeNulo() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();
        byte[] videoData = new byte[]{1, 2, 3};

        assertThatThrownBy(() -> extractor.extractFramesToZip(videoData, null, 1.0, 100))
                .isInstanceOf(FrameExtractionException.class)
                .hasMessageContaining("Nome do arquivo não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber intervalo inválido")
    void deveLancarExcecaoComIntervaloInvalido() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();
        byte[] videoData = new byte[]{1, 2, 3};

        assertThatThrownBy(() -> extractor.extractFramesToZip(videoData, "video.mp4", 0, 100))
                .isInstanceOf(FrameExtractionException.class)
                .hasMessageContaining("Intervalo deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber maxFrames inválido")
    void deveLancarExcecaoComMaxFramesInvalido() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();
        byte[] videoData = new byte[]{1, 2, 3};

        assertThatThrownBy(() -> extractor.extractFramesToZip(videoData, "video.mp4", 1.0, 0))
                .isInstanceOf(FrameExtractionException.class)
                .hasMessageContaining("Número máximo de frames deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve implementar a interface FrameExtractorPort")
    void deveImplementarInterface() {
        FakeFrameExtractor extractor = new FakeFrameExtractor();

        assertThat(extractor).isInstanceOf(FrameExtractorPort.class);
    }
}
