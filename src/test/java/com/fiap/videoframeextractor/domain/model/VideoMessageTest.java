package com.fiap.videoframeextractor.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VideoMessage")
class VideoMessageTest {

    @Nested
    @DisplayName("isValidForProcessing")
    class IsValidForProcessingTests {

        @Test
        @DisplayName("deve retornar true quando todos os campos obrigatórios estão presentes")
        void shouldReturnTrueWhenAllRequiredFieldsPresent() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-1")
                    .nmVideo("video.mp4")
                    .nmVideoPathOrigin("s3://bucket/video.mp4")
                    .nmPersonEmail("user@email.com")
                    .build();

            assertThat(msg.isValidForProcessing()).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando idVideoSend é nulo")
        void shouldReturnFalseWhenIdVideoSendIsNull() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend(null)
                    .nmVideo("video.mp4")
                    .nmVideoPathOrigin("s3://bucket/video.mp4")
                    .nmPersonEmail("user@email.com")
                    .build();

            assertThat(msg.isValidForProcessing()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nmVideo é nulo")
        void shouldReturnFalseWhenNmVideoIsNull() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-2")
                    .nmVideo(null)
                    .nmVideoPathOrigin("s3://bucket/video.mp4")
                    .nmPersonEmail("user@email.com")
                    .build();

            assertThat(msg.isValidForProcessing()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nmVideoPathOrigin é nulo")
        void shouldReturnFalseWhenVideoPathIsNull() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-3")
                    .nmVideo("video.mp4")
                    .nmVideoPathOrigin(null)
                    .nmPersonEmail("user@email.com")
                    .build();

            assertThat(msg.isValidForProcessing()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nmPersonEmail é nulo")
        void shouldReturnFalseWhenEmailIsNull() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-4")
                    .nmVideo("video.mp4")
                    .nmVideoPathOrigin("s3://bucket/video.mp4")
                    .nmPersonEmail(null)
                    .build();

            assertThat(msg.isValidForProcessing()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando todos os campos obrigatórios são nulos")
        void shouldReturnFalseWhenAllFieldsAreNull() {
            VideoMessage msg = new VideoMessage();

            assertThat(msg.isValidForProcessing()).isFalse();
        }

        @Test
        @DisplayName("deve retornar true mesmo sem campos opcionais (nmPersonName, nmVideoPathZip)")
        void shouldReturnTrueWithoutOptionalFields() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-5")
                    .nmVideo("v.mp4")
                    .nmVideoPathOrigin("s3://bucket/v.mp4")
                    .nmPersonEmail("a@b.com")
                    // nmPersonName e nmVideoPathZip intencionalmente omitidos
                    .build();

            assertThat(msg.isValidForProcessing()).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder e getters")
    class BuilderTests {

        @Test
        @DisplayName("deve construir VideoMessage com todos os campos usando builder")
        void shouldBuildVideoMessageWithAllFields() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("build-id")
                    .nmVideo("built.mp4")
                    .nmVideoPathOrigin("s3://bucket/built.mp4")
                    .nmPersonEmail("builder@test.com")
                    .nmPersonName("Builder User")
                    .cdVideoStatus("PENDING")
                    .nmVideoPathZip("s3://bucket/built.zip")
                    .build();

            assertThat(msg.getIdVideoSend()).isEqualTo("build-id");
            assertThat(msg.getNmVideo()).isEqualTo("built.mp4");
            assertThat(msg.getNmVideoPathOrigin()).isEqualTo("s3://bucket/built.mp4");
            assertThat(msg.getNmPersonEmail()).isEqualTo("builder@test.com");
            assertThat(msg.getNmPersonName()).isEqualTo("Builder User");
            assertThat(msg.getCdVideoStatus()).isEqualTo("PENDING");
            assertThat(msg.getNmVideoPathZip()).isEqualTo("s3://bucket/built.zip");
        }
    }
}
