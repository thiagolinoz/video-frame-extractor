package com.fiap.videoframeextractor.domain.services;

import com.fiap.videoframeextractor.domain.exceptions.VideoProcessingException;
import com.fiap.videoframeextractor.domain.model.VideoMessage;
import com.fiap.videoframeextractor.domain.model.VideoMetadata;
import com.fiap.videoframeextractor.domain.ports.out.FrameExtractorPort;
import com.fiap.videoframeextractor.domain.ports.out.VideoStoragePort;
import com.fiap.videoframeextractor.infrastructure.commons.mappers.VideoMessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FrameExtractionService")
class FrameExtractionServiceTest {

    @Mock
    private VideoStoragePort videoStoragePort;

    @Mock
    private FrameExtractorPort frameExtractorPort;

    @Mock
    private VideoMessageMapper videoMessageMapper;

    @InjectMocks
    private FrameExtractionService frameExtractionService;

    private VideoMessage validVideoMessage;

    @BeforeEach
    void setUp() {
        validVideoMessage = VideoMessage.builder()
                .idVideoSend("video-123")
                .nmVideo("test-video.mp4")
                .nmVideoPathOrigin("videos/test-video.mp4")
                .nmPersonEmail("user@example.com")
                .nmPersonName("User Test")
                .build();
    }

    // =========================================================
    // processVideoMessage
    // =========================================================
    @Nested
    @DisplayName("processVideoMessage")
    class ProcessVideoMessageTests {

        @Test
        @DisplayName("deve processar mensagem JSON válida com sucesso")
        void shouldProcessValidJsonMessage() {
            String json = "{\"idVideoSend\":\"video-123\"}";

            when(videoMessageMapper.toDomain(json)).thenReturn(validVideoMessage);
            when(videoStoragePort.videoExists(anyString())).thenReturn(true);
            when(videoStoragePort.getVideoMetadata(anyString()))
                    .thenReturn(VideoMetadata.builder().size(1024L).build());
            when(videoStoragePort.downloadVideo(anyString())).thenReturn(new byte[]{1, 2, 3});
            when(frameExtractorPort.extractFramesToZip(any(), anyString(), anyDouble(), anyInt()))
                    .thenReturn(new byte[]{4, 5, 6});
            when(videoStoragePort.uploadFramesZip(anyString(), any())).thenReturn("output/frames.zip");

            frameExtractionService.processVideoMessage(json);

            verify(videoMessageMapper).toDomain(json);
            verify(videoStoragePort).downloadVideo(validVideoMessage.getNmVideoPathOrigin());
        }

        @Test
        @DisplayName("deve lançar VideoProcessingException quando mapper falhar")
        void shouldThrowWhenMapperFails() {
            String invalidJson = "not-json";

            when(videoMessageMapper.toDomain(invalidJson))
                    .thenThrow(new RuntimeException("parse error"));

            assertThatThrownBy(() -> frameExtractionService.processVideoMessage(invalidJson))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Failed to process video message");
        }
    }

    // =========================================================
    // processVideo
    // =========================================================
    @Nested
    @DisplayName("processVideo")
    class ProcessVideoTests {

        @Test
        @DisplayName("deve retornar zipPath quando processamento é bem-sucedido")
        void shouldReturnZipPathOnSuccess() {
            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenReturn(VideoMetadata.builder().size(10_000L).build());
            when(videoStoragePort.downloadVideo("videos/test-video.mp4"))
                    .thenReturn(new byte[100]);
            when(frameExtractorPort.extractFramesToZip(any(), eq("test-video.mp4"), eq(1.0), eq(100)))
                    .thenReturn(new byte[200]);
            when(videoStoragePort.uploadFramesZip("video-123", new byte[200]))
                    .thenReturn("output/video-123/frames.zip");

            String result = frameExtractionService.processVideo(validVideoMessage);

            assertThat(result).isEqualTo("output/video-123/frames.zip");
            verify(videoStoragePort).downloadVideo("videos/test-video.mp4");
            verify(frameExtractorPort).extractFramesToZip(any(), eq("test-video.mp4"), eq(1.0), eq(100));
            verify(videoStoragePort).uploadFramesZip(eq("video-123"), any());
        }

        @Test
        @DisplayName("deve lançar VideoProcessingException quando vídeo não existe no S3")
        void shouldThrowWhenVideoNotFoundInS3() {
            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(false);

            assertThatThrownBy(() -> frameExtractionService.processVideo(validVideoMessage))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Vídeo não encontrado no S3");
        }

        @Test
        @DisplayName("deve lançar VideoProcessingException quando arquivo ultrapassa 100MB")
        void shouldThrowWhenFileSizeExceedsLimit() {
            long oversizedBytes = 101L * 1024 * 1024;

            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenReturn(VideoMetadata.builder().size(oversizedBytes).build());

            assertThatThrownBy(() -> frameExtractionService.processVideo(validVideoMessage))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Arquivo muito grande");
        }

        @Test
        @DisplayName("deve continuar processamento quando validação de tamanho falha inesperadamente")
        void shouldContinueWhenMetadataCheckThrowsGenericException() {
            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenThrow(new RuntimeException("S3 metadata error"));
            when(videoStoragePort.downloadVideo("videos/test-video.mp4"))
                    .thenReturn(new byte[100]);
            when(frameExtractorPort.extractFramesToZip(any(), anyString(), anyDouble(), anyInt()))
                    .thenReturn(new byte[50]);
            when(videoStoragePort.uploadFramesZip(anyString(), any()))
                    .thenReturn("output/frames.zip");

            String result = frameExtractionService.processVideo(validVideoMessage);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("deve aceitar arquivo exatamente no limite de 100MB")
        void shouldAcceptFileAtExactSizeLimit() {
            long exactLimit = 100L * 1024 * 1024;

            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenReturn(VideoMetadata.builder().size(exactLimit).build());
            when(videoStoragePort.downloadVideo("videos/test-video.mp4"))
                    .thenReturn(new byte[100]);
            when(frameExtractorPort.extractFramesToZip(any(), anyString(), anyDouble(), anyInt()))
                    .thenReturn(new byte[50]);
            when(videoStoragePort.uploadFramesZip(anyString(), any()))
                    .thenReturn("output/frames.zip");

            String result = frameExtractionService.processVideo(validVideoMessage);

            assertThat(result).isEqualTo("output/frames.zip");
        }

        @Test
        @DisplayName("deve lançar VideoProcessingException quando download falha")
        void shouldThrowWhenDownloadFails() {
            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenReturn(VideoMetadata.builder().size(500L).build());
            when(videoStoragePort.downloadVideo("videos/test-video.mp4"))
                    .thenThrow(new RuntimeException("Download error"));

            assertThatThrownBy(() -> frameExtractionService.processVideo(validVideoMessage))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Falha no processamento");
        }

        @Test
        @DisplayName("deve lançar VideoProcessingException quando extração de frames falha")
        void shouldThrowWhenFrameExtractionFails() {
            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenReturn(VideoMetadata.builder().size(500L).build());
            when(videoStoragePort.downloadVideo("videos/test-video.mp4"))
                    .thenReturn(new byte[100]);
            when(frameExtractorPort.extractFramesToZip(any(), anyString(), anyDouble(), anyInt()))
                    .thenThrow(new RuntimeException("FFmpeg error"));

            assertThatThrownBy(() -> frameExtractionService.processVideo(validVideoMessage))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Falha no processamento");
        }

        @Test
        @DisplayName("deve lançar VideoProcessingException quando upload do ZIP falha")
        void shouldThrowWhenUploadFails() {
            when(videoStoragePort.videoExists("videos/test-video.mp4")).thenReturn(true);
            when(videoStoragePort.getVideoMetadata("videos/test-video.mp4"))
                    .thenReturn(VideoMetadata.builder().size(500L).build());
            when(videoStoragePort.downloadVideo("videos/test-video.mp4"))
                    .thenReturn(new byte[100]);
            when(frameExtractorPort.extractFramesToZip(any(), anyString(), anyDouble(), anyInt()))
                    .thenReturn(new byte[50]);
            when(videoStoragePort.uploadFramesZip(anyString(), any()))
                    .thenThrow(new RuntimeException("S3 upload error"));

            assertThatThrownBy(() -> frameExtractionService.processVideo(validVideoMessage))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Falha no processamento");
        }
    }

    // =========================================================
    // Validação de formato
    // =========================================================
    @Nested
    @DisplayName("validateVideoFormat")
    class ValidateVideoFormatTests {

        @ParameterizedTest(name = "formato suportado: {0}")
        @ValueSource(strings = {"video.mp4", "video.avi", "video.mov", "video.mkv",
                                "VIDEO.MP4", "VIDEO.AVI", "VIDEO.MOV", "VIDEO.MKV"})
        @DisplayName("deve aceitar formatos suportados")
        void shouldAcceptSupportedFormats(String fileName) {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-1")
                    .nmVideo(fileName)
                    .nmVideoPathOrigin("path/" + fileName)
                    .nmPersonEmail("a@b.com")
                    .build();

            when(videoStoragePort.videoExists(anyString())).thenReturn(true);
            when(videoStoragePort.getVideoMetadata(anyString()))
                    .thenReturn(VideoMetadata.builder().size(1000L).build());
            when(videoStoragePort.downloadVideo(anyString())).thenReturn(new byte[10]);
            when(frameExtractorPort.extractFramesToZip(any(), anyString(), anyDouble(), anyInt()))
                    .thenReturn(new byte[5]);
            when(videoStoragePort.uploadFramesZip(anyString(), any())).thenReturn("zip/path");

            String result = frameExtractionService.processVideo(msg);

            assertThat(result).isEqualTo("zip/path");
        }

        @ParameterizedTest(name = "formato não suportado: {0}")
        @ValueSource(strings = {"video.wmv", "video.flv", "video.webm", "video.txt", "video.pdf"})
        @DisplayName("deve rejeitar formatos não suportados")
        void shouldRejectUnsupportedFormats(String fileName) {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-2")
                    .nmVideo(fileName)
                    .nmVideoPathOrigin("path/" + fileName)
                    .nmPersonEmail("a@b.com")
                    .build();

            assertThatThrownBy(() -> frameExtractionService.processVideo(msg))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("não suportado");
        }

        @Test
        @DisplayName("deve lançar exceção quando nome do arquivo é nulo")
        void shouldThrowWhenFileNameIsNull() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-3")
                    .nmVideo(null)
                    .nmVideoPathOrigin("path/file")
                    .nmPersonEmail("a@b.com")
                    .build();

            assertThatThrownBy(() -> frameExtractionService.processVideo(msg))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Nome do arquivo não informado");
        }

        @Test
        @DisplayName("deve lançar exceção quando nome do arquivo está vazio")
        void shouldThrowWhenFileNameIsEmpty() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-4")
                    .nmVideo("")
                    .nmVideoPathOrigin("path/file")
                    .nmPersonEmail("a@b.com")
                    .build();

            assertThatThrownBy(() -> frameExtractionService.processVideo(msg))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("Nome do arquivo não informado");
        }

        @Test
        @DisplayName("deve lançar exceção quando arquivo não possui extensão")
        void shouldThrowWhenFileHasNoExtension() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-5")
                    .nmVideo("videoSemExtensao")
                    .nmVideoPathOrigin("path/videoSemExtensao")
                    .nmPersonEmail("a@b.com")
                    .build();

            assertThatThrownBy(() -> frameExtractionService.processVideo(msg))
                    .isInstanceOf(VideoProcessingException.class)
                    .hasMessageContaining("não suportado");
        }
    }
}
