package com.fiap.videoframeextractor.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@DisplayName("VideoStatusMessage")
class VideoStatusMessageTest {

    @Nested
    @DisplayName("factory method processing()")
    class ProcessingFactoryTests {

        @Test
        @DisplayName("deve criar mensagem com status PROCESSING e campos corretos")
        void shouldCreateProcessingMessage() {
            VideoStatusMessage msg = VideoStatusMessage.processing(
                    "v-1", "user@fiap.com", "clip.mp4", "User Name");

            assertThat(msg.getIdVideoSend()).isEqualTo("v-1");
            assertThat(msg.getNmPersonEmail()).isEqualTo("user@fiap.com");
            assertThat(msg.getNmVideo()).isEqualTo("clip.mp4");
            assertThat(msg.getNmPersonName()).isEqualTo("User Name");
            assertThat(msg.getCdVideoStatus()).isEqualTo("PROCESSING");
            assertThat(msg.getNmVideoPathZip()).isNull();
            assertThat(msg.getDateTimeVideoProcessCompleted()).isNull();
            assertThat(msg.getErrorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("factory method completed()")
    class CompletedFactoryTests {

        @Test
        @DisplayName("deve criar mensagem com status COMPLETED, zip path e data de conclusão")
        void shouldCreateCompletedMessage() {
            Instant before = Instant.now().minusSeconds(1);

            VideoStatusMessage msg = VideoStatusMessage.completed(
                    "v-2", "user@fiap.com", "clip.mp4", "User Name", "s3://bucket/clip.zip");

            Instant after = Instant.now().plusSeconds(1);

            assertThat(msg.getCdVideoStatus()).isEqualTo("COMPLETED");
            assertThat(msg.getNmVideoPathZip()).isEqualTo("s3://bucket/clip.zip");
            assertThat(msg.getDateTimeVideoProcessCompleted()).isNotNull();
            assertThat(msg.getDateTimeVideoProcessCompleted())
                    .isBetween(Date.from(before), Date.from(after));
        }

        @Test
        @DisplayName("deve preservar todos os campos de identificação no status COMPLETED")
        void shouldPreserveIdentificationFieldsInCompleted() {
            VideoStatusMessage msg = VideoStatusMessage.completed(
                    "v-3", "john@fiap.com", "movie.mkv", "John", "s3://zip");

            assertThat(msg.getIdVideoSend()).isEqualTo("v-3");
            assertThat(msg.getNmPersonEmail()).isEqualTo("john@fiap.com");
            assertThat(msg.getNmVideo()).isEqualTo("movie.mkv");
            assertThat(msg.getNmPersonName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("factory method error()")
    class ErrorFactoryTests {

        @Test
        @DisplayName("deve criar mensagem com status PROCESS_ERROR")
        void shouldCreateErrorMessage() {
            VideoStatusMessage msg = VideoStatusMessage.error(
                    "v-4", "user@fiap.com", "clip.mp4", "User Name");

            assertThat(msg.getCdVideoStatus()).isEqualTo("PROCESS_ERROR");
            assertThat(msg.getIdVideoSend()).isEqualTo("v-4");
            assertThat(msg.getNmPersonEmail()).isEqualTo("user@fiap.com");
            assertThat(msg.getNmVideo()).isEqualTo("clip.mp4");
            assertThat(msg.getNmPersonName()).isEqualTo("User Name");
            assertThat(msg.getNmVideoPathZip()).isNull();
            assertThat(msg.getDateTimeVideoProcessCompleted()).isNull();
        }

        @Test
        @DisplayName("deve permitir definir errorMessage após criação")
        void shouldAllowSettingErrorMessageAfterCreation() {
            VideoStatusMessage msg = VideoStatusMessage.error("v-5", "e@e.com", "v.mp4", "E");
            msg.setErrorMessage("Disk full");

            assertThat(msg.getErrorMessage()).isEqualTo("Disk full");
        }
    }
}
