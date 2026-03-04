package com.fiap.videoframeextractor.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("FFmpegConfig - Testes Unitários")
class FFmpegConfigTest {

    private FFmpegConfig ffmpegConfig;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ffmpegConfig = new FFmpegConfig();
    }

    @Test
    @DisplayName("Deve inicializar sem erros quando ffmpeg não está no path informado")
    void deveInicializarSemErrosQuandoFFmpegNaoEncontrado() {
        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", "/caminho/inexistente/ffmpeg");
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", tempDir.toString());

        assertThatCode(() -> ffmpegConfig.init()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve criar diretório de saída durante inicialização quando não existe")
    void deveCriarDiretorioDeSaidaDuranteInit() {
        String newDir = tempDir.resolve("frames-ffmpeg").toString();
        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", "ffmpeg");
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", newDir);

        ffmpegConfig.init();

        assertThat(new File(newDir)).exists().isDirectory();
    }

    @Test
    @DisplayName("Deve inicializar sem erros quando diretório de saída já existe")
    void deveInicializarSemErrosComDiretorioExistente() {
        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", "ffmpeg");
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", tempDir.toString());

        assertThatCode(() -> ffmpegConfig.init()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve retornar o path do ffmpeg configurado")
    void deveRetornarPathDoFFmpeg() {
        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", "/usr/bin/ffmpeg");
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", tempDir.toString());

        String path = ffmpegConfig.getFfmpegPath();

        assertThat(path).isEqualTo("/usr/bin/ffmpeg");
    }

    @Test
    @DisplayName("Deve retornar o diretório de saída configurado")
    void deveRetornarOutputDirectory() {
        String expectedDir = tempDir.toString();
        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", "ffmpeg");
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", expectedDir);

        String dir = ffmpegConfig.getOutputDirectory();

        assertThat(dir).isEqualTo(expectedDir);
    }

    @Test
    @DisplayName("Deve inicializar e reconhecer ffmpeg como arquivo existente no sistema")
    void deveReconhecerArquivoFFmpegExistente() throws Exception {
        File fakeFFmpeg = tempDir.resolve("ffmpeg").toFile();
        fakeFFmpeg.createNewFile();

        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", fakeFFmpeg.getAbsolutePath());
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", tempDir.toString());

        assertThatCode(() -> ffmpegConfig.init()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve criar diretórios aninhados durante inicialização")
    void deveCriarDiretoriosAninhados() {
        String nestedDir = tempDir.resolve("a/b/frames").toString();
        ReflectionTestUtils.setField(ffmpegConfig, "ffmpegPath", "ffmpeg");
        ReflectionTestUtils.setField(ffmpegConfig, "outputDirectory", nestedDir);

        ffmpegConfig.init();

        assertThat(new File(nestedDir)).exists().isDirectory();
    }
}
