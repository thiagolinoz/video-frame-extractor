package com.fiap.videoframeextractor.configuration;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("AwsS3Config - Testes Unitários")
class AwsS3ConfigTest {

    private AwsS3Config awsS3Config;

    @BeforeEach
    void setUp() {
        awsS3Config = new AwsS3Config();
    }

    @Test
    @DisplayName("Deve criar cliente S3 com região padrão sem endpoint customizado")
    void deveCriarClienteS3ComRegiaoPadrao() {
        ReflectionTestUtils.setField(awsS3Config, "region", "us-east-1");
        ReflectionTestUtils.setField(awsS3Config, "s3Endpoint", "");
        ReflectionTestUtils.setField(awsS3Config, "pathStyleAccessEnabled", false);

        assertThatCode(() -> {
            AmazonS3 client = awsS3Config.amazonS3Client();
            assertThat(client).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve criar cliente S3 com endpoint customizado (LocalStack)")
    void deveCriarClienteS3ComEndpointCustomizado() {
        ReflectionTestUtils.setField(awsS3Config, "region", "us-east-1");
        ReflectionTestUtils.setField(awsS3Config, "s3Endpoint", "http://localhost:4566");
        ReflectionTestUtils.setField(awsS3Config, "pathStyleAccessEnabled", true);

        assertThatCode(() -> {
            AmazonS3 client = awsS3Config.amazonS3Client();
            assertThat(client).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve criar cliente S3 com path-style access habilitado")
    void deveCriarClienteS3ComPathStyleAccess() {
        ReflectionTestUtils.setField(awsS3Config, "region", "sa-east-1");
        ReflectionTestUtils.setField(awsS3Config, "s3Endpoint", "");
        ReflectionTestUtils.setField(awsS3Config, "pathStyleAccessEnabled", true);

        assertThatCode(() -> {
            AmazonS3 client = awsS3Config.amazonS3Client();
            assertThat(client).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve ignorar endpoint com espaços em branco e usar região")
    void deveIgnorarEndpointComEspacos() {
        ReflectionTestUtils.setField(awsS3Config, "region", "us-east-1");
        ReflectionTestUtils.setField(awsS3Config, "s3Endpoint", "   ");
        ReflectionTestUtils.setField(awsS3Config, "pathStyleAccessEnabled", false);

        assertThatCode(() -> {
            AmazonS3 client = awsS3Config.amazonS3Client();
            assertThat(client).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve criar cliente S3 com região diferente")
    void deveCriarClienteS3ComRegiaoEuWest() {
        ReflectionTestUtils.setField(awsS3Config, "region", "eu-west-1");
        ReflectionTestUtils.setField(awsS3Config, "s3Endpoint", "");
        ReflectionTestUtils.setField(awsS3Config, "pathStyleAccessEnabled", false);

        assertThatCode(() -> {
            AmazonS3 client = awsS3Config.amazonS3Client();
            assertThat(client).isNotNull();
        }).doesNotThrowAnyException();
    }
}
