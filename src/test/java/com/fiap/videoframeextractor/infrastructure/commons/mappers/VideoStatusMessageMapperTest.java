package com.fiap.videoframeextractor.infrastructure.commons.mappers;

import com.fiap.videoframeextractor.domain.model.VideoStatusMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VideoStatusMessageMapper")
class VideoStatusMessageMapperTest {

    private VideoStatusMessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VideoStatusMessageMapper();
    }

    @Nested
    @DisplayName("toJson")
    class ToJsonTests {

        @Test
        @DisplayName("deve serializar VideoStatusMessage de PROCESSING corretamente")
        void shouldSerializeProcessingStatus() {
            VideoStatusMessage msg = VideoStatusMessage.processing(
                    "vid-100", "user@fiap.com", "video.mp4", "User");

            String json = mapper.toJson(msg);

            assertThat(json).contains("\"cdVideoStatus\":\"PROCESSING\"");
            assertThat(json).contains("\"idVideoSend\":\"vid-100\"");
            assertThat(json).contains("\"nmPersonEmail\":\"user@fiap.com\"");
        }

        @Test
        @DisplayName("deve serializar VideoStatusMessage de COMPLETED com zip path e data")
        void shouldSerializeCompletedStatusWithZipPath() {
            VideoStatusMessage msg = VideoStatusMessage.completed(
                    "vid-200", "user@fiap.com", "video.mp4", "User", "s3://bucket/frames.zip");

            String json = mapper.toJson(msg);

            assertThat(json).contains("\"cdVideoStatus\":\"COMPLETED\"");
            assertThat(json).contains("s3://bucket/frames.zip");
            assertThat(json).contains("dateTimeVideoProcessCompleted");
        }

        @Test
        @DisplayName("deve serializar VideoStatusMessage de PROCESS_ERROR corretamente")
        void shouldSerializeErrorStatus() {
            VideoStatusMessage msg = VideoStatusMessage.error(
                    "vid-300", "user@fiap.com", "video.mp4", "User");
            msg.setErrorMessage("Something went wrong");

            String json = mapper.toJson(msg);

            assertThat(json).contains("\"cdVideoStatus\":\"PROCESS_ERROR\"");
            assertThat(json).contains("Something went wrong");
        }
    }

    @Nested
    @DisplayName("fromJson")
    class FromJsonTests {

        @Test
        @DisplayName("deve deserializar JSON para VideoStatusMessage corretamente")
        void shouldDeserializeJsonToVideoStatusMessage() {
            String json = """
                    {
                      "idVideoSend": "vid-400",
                      "nmPersonEmail": "test@email.com",
                      "cdVideoStatus": "COMPLETED",
                      "nmVideo": "test.mp4",
                      "nmPersonName": "Test Person",
                      "nmVideoPathZip": "s3://bucket/test.zip"
                    }
                    """;

            VideoStatusMessage result = mapper.fromJson(json);

            assertThat(result.getIdVideoSend()).isEqualTo("vid-400");
            assertThat(result.getCdVideoStatus()).isEqualTo("COMPLETED");
            assertThat(result.getNmPersonEmail()).isEqualTo("test@email.com");
            assertThat(result.getNmVideoPathZip()).isEqualTo("s3://bucket/test.zip");
        }

        @Test
        @DisplayName("deve lançar RuntimeException para JSON inválido")
        void shouldThrowForInvalidJson() {
            assertThatThrownBy(() -> mapper.fromJson("invalid-json"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to parse status message");
        }

        @Test
        @DisplayName("deve produzir JSON que pode ser deserializado de volta ao objeto original")
        void shouldProduceRoundTripJson() {
            VideoStatusMessage original = VideoStatusMessage.builder()
                    .idVideoSend("rt-500")
                    .nmPersonEmail("rt@fiap.com")
                    .cdVideoStatus("PROCESSING")
                    .nmVideo("roundtrip.mkv")
                    .nmPersonName("Round Trip")
                    .build();

            String json = mapper.toJson(original);
            VideoStatusMessage deserialized = mapper.fromJson(json);

            assertThat(deserialized.getIdVideoSend()).isEqualTo(original.getIdVideoSend());
            assertThat(deserialized.getCdVideoStatus()).isEqualTo(original.getCdVideoStatus());
            assertThat(deserialized.getNmPersonEmail()).isEqualTo(original.getNmPersonEmail());
        }
    }
}
