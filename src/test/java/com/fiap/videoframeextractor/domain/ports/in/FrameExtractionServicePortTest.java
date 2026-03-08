package com.fiap.videoframeextractor.domain.ports.in;

import com.fiap.videoframeextractor.domain.model.VideoMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FrameExtractionServicePort - Testes de Contrato")
class FrameExtractionServicePortTest {

    // Implementação fake para validar o contrato da interface
    static class FakeFrameExtractionService implements FrameExtractionServicePort {

        private boolean processVideoMessageCalled = false;
        private boolean processVideoCalled = false;
        private String lastMessage;
        private VideoMessage lastVideoMessage;

        @Override
        public void processVideoMessage(String message) {
            if (message == null) {
                throw new IllegalArgumentException("Mensagem não pode ser nula");
            }
            this.processVideoMessageCalled = true;
            this.lastMessage = message;
        }

        @Override
        public String processVideo(VideoMessage videoMessage) {
            if (videoMessage == null) {
                throw new IllegalArgumentException("VideoMessage não pode ser nulo");
            }
            this.processVideoCalled = true;
            this.lastVideoMessage = videoMessage;
            return "frames/" + videoMessage.getIdVideoSend() + ".zip";
        }
    }

    @Test
    @DisplayName("Deve processar mensagem de vídeo via processVideoMessage")
    void deveProcessarMensagemDeVideo() {
        FakeFrameExtractionService service = new FakeFrameExtractionService();
        String mensagemJson = "{\"idVideoSend\":\"123\",\"nmVideo\":\"video.mp4\"}";

        service.processVideoMessage(mensagemJson);

        assertThat(service.processVideoMessageCalled).isTrue();
        assertThat(service.lastMessage).isEqualTo(mensagemJson);
    }

    @Test
    @DisplayName("Deve processar VideoMessage e retornar caminho do ZIP")
    void deveProcessarVideoMessageERetornarCaminhoZip() {
        FakeFrameExtractionService service = new FakeFrameExtractionService();
        VideoMessage videoMessage = VideoMessage.builder()
                .idVideoSend("video-456")
                .nmVideo("filme.mp4")
                .nmPersonEmail("user@email.com")
                .nmVideoPathOrigin("videos/filme.mp4")
                .build();

        String resultado = service.processVideo(videoMessage);

        assertThat(service.processVideoCalled).isTrue();
        assertThat(resultado).isEqualTo("frames/video-456.zip");
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber mensagem nula em processVideoMessage")
    void deveLancarExcecaoComMensagemNula() {
        FakeFrameExtractionService service = new FakeFrameExtractionService();

        assertThatThrownBy(() -> service.processVideoMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mensagem não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber VideoMessage nulo em processVideo")
    void deveLancarExcecaoComVideoMessageNulo() {
        FakeFrameExtractionService service = new FakeFrameExtractionService();

        assertThatThrownBy(() -> service.processVideo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VideoMessage não pode ser nulo");
    }

    @Test
    @DisplayName("Deve implementar a interface FrameExtractionServicePort")
    void deveImplementarInterface() {
        FakeFrameExtractionService service = new FakeFrameExtractionService();

        assertThat(service).isInstanceOf(FrameExtractionServicePort.class);
    }
}
