package com.fiap.videoframeextractor.configuration;

import org.junit.jupiter.api.AfterEach;
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
@DisplayName("DirectoryInitializer - Testes Unitários")
class DirectoryInitializerTest {

    private DirectoryInitializer directoryInitializer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        directoryInitializer = new DirectoryInitializer();
    }

    @Test
    @DisplayName("Deve criar diretório de saída quando não existe")
    void deveCriarDiretorioQuandoNaoExiste() throws Exception {
        String newDir = tempDir.resolve("frames-output").toString();
        ReflectionTestUtils.setField(directoryInitializer, "outputDirectory", newDir);

        directoryInitializer.run();

        assertThat(new File(newDir)).exists().isDirectory();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando diretório já existe")
    void naoDeveLancarExcecaoQuandoDiretorioJaExiste() {
        String existingDir = tempDir.toString();
        ReflectionTestUtils.setField(directoryInitializer, "outputDirectory", existingDir);

        assertThatCode(() -> directoryInitializer.run()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve executar sem erros quando diretório já existe")
    void deveExecutarSemErrosComDiretorioExistente() throws Exception {
        String existingDir = tempDir.toString();
        ReflectionTestUtils.setField(directoryInitializer, "outputDirectory", existingDir);

        directoryInitializer.run();

        assertThat(new File(existingDir)).exists().isDirectory();
    }

    @Test
    @DisplayName("Deve criar diretórios aninhados")
    void deveCriarDiretoriosAninhados() throws Exception {
        String nestedDir = tempDir.resolve("a/b/c/frames").toString();
        ReflectionTestUtils.setField(directoryInitializer, "outputDirectory", nestedDir);

        directoryInitializer.run();

        assertThat(new File(nestedDir)).exists().isDirectory();
    }

    @Test
    @DisplayName("Deve aceitar args vazios no método run")
    void deveAceitarArgsVazios() {
        ReflectionTestUtils.setField(directoryInitializer, "outputDirectory", tempDir.toString());

        assertThatCode(() -> directoryInitializer.run(new String[]{}))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve aceitar args nulos no método run")
    void deveAceitarArgsComValores() throws Exception {
        ReflectionTestUtils.setField(directoryInitializer, "outputDirectory", tempDir.toString());

        assertThatCode(() -> directoryInitializer.run("arg1", "arg2"))
                .doesNotThrowAnyException();
    }
}
