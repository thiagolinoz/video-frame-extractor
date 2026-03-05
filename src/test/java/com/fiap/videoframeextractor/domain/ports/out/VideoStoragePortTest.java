package com.fiap.videoframeextractor.domain.ports.out;

import com.fiap.videoframeextractor.domain.exceptions.StorageException;
import com.fiap.videoframeextractor.domain.exceptions.VideoNotFoundException;
import com.fiap.videoframeextractor.domain.model.VideoMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VideoStoragePort - Testes de Contrato")
class VideoStoragePortTest {

    // Implementação fake para validar o contrato da interface
    static class FakeVideoStorage implements VideoStoragePort {

        private final Map<String, byte[]> storage = new HashMap<>();

        public void addVideo(String path, byte[] data) {
            storage.put(path, data);
        }

        @Override
        public boolean videoExists(String videoPath) {
            if (videoPath == null) {
                throw new StorageException("Caminho do vídeo não pode ser nulo");
            }
            return storage.containsKey(videoPath);
        }

        @Override
        public byte[] downloadVideo(String videoPath) {
            if (!storage.containsKey(videoPath)) {
                throw new VideoNotFoundException("Vídeo não encontrado: " + videoPath);
            }
            return storage.get(videoPath);
        }

        @Override
        public String uploadFramesZip(String videoId, byte[] framesZip) {
            if (framesZip == null || framesZip.length == 0) {
                throw new StorageException("Dados do ZIP não podem ser vazios");
            }
            String zipPath = "frames/" + videoId + ".zip";
            storage.put(zipPath, framesZip);
            return zipPath;
        }

        @Override
        public VideoMetadata getVideoMetadata(String videoPath) {
            if (!storage.containsKey(videoPath)) {
                throw new VideoNotFoundException("Vídeo não encontrado: " + videoPath);
            }
            byte[] data = storage.get(videoPath);
            return VideoMetadata.builder()
                    .fileName(videoPath)
                    .size(data.length)
                    .contentType("video/mp4")
                    .path(videoPath)
                    .build();
        }
    }

    @Test
    @DisplayName("Deve retornar true quando vídeo existe no storage")
    void deveRetornarTrueQuandoVideoExiste() {
        FakeVideoStorage storage = new FakeVideoStorage();
        storage.addVideo("videos/video.mp4", new byte[]{1, 2, 3});

        assertThat(storage.videoExists("videos/video.mp4")).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando vídeo não existe no storage")
    void deveRetornarFalseQuandoVideoNaoExiste() {
        FakeVideoStorage storage = new FakeVideoStorage();

        assertThat(storage.videoExists("videos/inexistente.mp4")).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção ao verificar existência com caminho nulo")
    void deveLancarExcecaoComCaminhoNuloEmVideoExists() {
        FakeVideoStorage storage = new FakeVideoStorage();

        assertThatThrownBy(() -> storage.videoExists(null))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Caminho do vídeo não pode ser nulo");
    }

    @Test
    @DisplayName("Deve fazer download do vídeo e retornar seus bytes")
    void deveFazerDownloadDoVideo() {
        FakeVideoStorage storage = new FakeVideoStorage();
        byte[] videoData = {10, 20, 30, 40};
        storage.addVideo("videos/teste.mp4", videoData);

        byte[] resultado = storage.downloadVideo("videos/teste.mp4");

        assertThat(resultado).isEqualTo(videoData);
    }

    @Test
    @DisplayName("Deve lançar VideoNotFoundException ao baixar vídeo inexistente")
    void deveLancarExcecaoAoBaixarVideoInexistente() {
        FakeVideoStorage storage = new FakeVideoStorage();

        assertThatThrownBy(() -> storage.downloadVideo("videos/naoexiste.mp4"))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessageContaining("Vídeo não encontrado: videos/naoexiste.mp4");
    }

    @Test
    @DisplayName("Deve fazer upload do ZIP e retornar o caminho")
    void deveFazerUploadDoZip() {
        FakeVideoStorage storage = new FakeVideoStorage();
        byte[] zipData = {50, 60, 70};

        String caminho = storage.uploadFramesZip("video-123", zipData);

        assertThat(caminho).isEqualTo("frames/video-123.zip");
        assertThat(storage.videoExists("frames/video-123.zip")).isTrue();
    }

    @Test
    @DisplayName("Deve lançar StorageException ao fazer upload com ZIP vazio")
    void deveLancarExcecaoComZipVazio() {
        FakeVideoStorage storage = new FakeVideoStorage();

        assertThatThrownBy(() -> storage.uploadFramesZip("video-123", new byte[0]))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Dados do ZIP não podem ser vazios");
    }

    @Test
    @DisplayName("Deve retornar metadata correta do vídeo")
    void deveRetornarMetadataCorreta() {
        FakeVideoStorage storage = new FakeVideoStorage();
        byte[] videoData = new byte[512];
        storage.addVideo("videos/meta.mp4", videoData);

        VideoMetadata metadata = storage.getVideoMetadata("videos/meta.mp4");

        assertThat(metadata.getSize()).isEqualTo(512);
        assertThat(metadata.getContentType()).isEqualTo("video/mp4");
        assertThat(metadata.getPath()).isEqualTo("videos/meta.mp4");
    }

    @Test
    @DisplayName("Deve lançar VideoNotFoundException ao buscar metadata de vídeo inexistente")
    void deveLancarExcecaoAoBuscarMetadataDeVideoInexistente() {
        FakeVideoStorage storage = new FakeVideoStorage();

        assertThatThrownBy(() -> storage.getVideoMetadata("videos/naoexiste.mp4"))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    @DisplayName("Deve implementar a interface VideoStoragePort")
    void deveImplementarInterface() {
        FakeVideoStorage storage = new FakeVideoStorage();

        assertThat(storage).isInstanceOf(VideoStoragePort.class);
    }
}
