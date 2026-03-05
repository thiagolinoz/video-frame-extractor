package com.fiap.videoframeextractor.infrastructure.commons.mappers;

import com.fiap.videoframeextractor.domain.model.VideoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VideoMessageMapper")
class VideoMessageMapperTest {

    private VideoMessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VideoMessageMapper();
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomainTests {

        @Test
        @DisplayName("deve converter JSON completo para VideoMessage")
        void shouldConvertFullJsonToVideoMessage() {
            String json = """
                    {
                      "idVideoSend": "abc-123",
                      "nmVideo": "movie.mp4",
                      "nmVideoPathOrigin": "s3://bucket/movie.mp4",
                      "nmPersonEmail": "sarah@fiap.com",
                      "nmPersonName": "Sarah"
                    }
                    """;

            VideoMessage result = mapper.toDomain(json);

            assertThat(result.getIdVideoSend()).isEqualTo("abc-123");
            assertThat(result.getNmVideo()).isEqualTo("movie.mp4");
            assertThat(result.getNmVideoPathOrigin()).isEqualTo("s3://bucket/movie.mp4");
            assertThat(result.getNmPersonEmail()).isEqualTo("sarah@fiap.com");
            assertThat(result.getNmPersonName()).isEqualTo("Sarah");
        }

        @Test
        @DisplayName("deve ignorar campos desconhecidos no JSON")
        void shouldIgnoreUnknownFields() {
            String json = """
                    {
                      "idVideoSend": "xyz-456",
                      "nmVideo": "video.mp4",
                      "nmVideoPathOrigin": "s3://bucket/video.mp4",
                      "nmPersonEmail": "user@test.com",
                      "unknownField": "should-be-ignored",
                      "anotherUnknown": 42
                    }
                    """;

            VideoMessage result = mapper.toDomain(json);

            assertThat(result.getIdVideoSend()).isEqualTo("xyz-456");
        }

        @Test
        @DisplayName("deve retornar campos nulos quando ausentes no JSON")
        void shouldReturnNullForMissingFields() {
            String json = """
                    {
                      "idVideoSend": "minimal-id"
                    }
                    """;

            VideoMessage result = mapper.toDomain(json);

            assertThat(result.getIdVideoSend()).isEqualTo("minimal-id");
            assertThat(result.getNmVideo()).isNull();
            assertThat(result.getNmPersonEmail()).isNull();
        }

        @Test
        @DisplayName("deve lançar RuntimeException para JSON inválido")
        void shouldThrowRuntimeExceptionForInvalidJson() {
            String badJson = "this is not json";

            assertThatThrownBy(() -> mapper.toDomain(badJson))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to parse video message");
        }

        @Test
        @DisplayName("deve lançar RuntimeException para String nula")
        void shouldThrowForNullInput() {
            assertThatThrownBy(() -> mapper.toDomain(null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("toJson")
    class ToJsonTests {

        @Test
        @DisplayName("deve serializar VideoMessage para JSON com campos esperados")
        void shouldSerializeVideoMessageToJson() {
            VideoMessage msg = VideoMessage.builder()
                    .idVideoSend("id-001")
                    .nmVideo("clip.mp4")
                    .nmPersonEmail("dev@fiap.com")
                    .nmVideoPathOrigin("s3://bucket/clip.mp4")
                    .build();

            String json = mapper.toJson(msg);

            assertThat(json).contains("\"idVideoSend\":\"id-001\"");
            assertThat(json).contains("\"nmVideo\":\"clip.mp4\"");
            assertThat(json).contains("\"nmPersonEmail\":\"dev@fiap.com\"");
        }

        @Test
        @DisplayName("deve produzir JSON que pode ser deserializado de volta ao objeto original")
        void shouldProduceRoundTripJson() {
            VideoMessage original = VideoMessage.builder()
                    .idVideoSend("round-trip-id")
                    .nmVideo("roundtrip.avi")
                    .nmPersonEmail("round@trip.com")
                    .nmVideoPathOrigin("s3://bucket/roundtrip.avi")
                    .nmPersonName("Round Trip User")
                    .build();

            String json = mapper.toJson(original);
            VideoMessage deserialized = mapper.toDomain(json);

            assertThat(deserialized.getIdVideoSend()).isEqualTo(original.getIdVideoSend());
            assertThat(deserialized.getNmVideo()).isEqualTo(original.getNmVideo());
            assertThat(deserialized.getNmPersonEmail()).isEqualTo(original.getNmPersonEmail());
            assertThat(deserialized.getNmPersonName()).isEqualTo(original.getNmPersonName());
        }
    }
}
