package com.summitbra.videoframeextractor.application.port.out;

import java.nio.file.Path;

public interface FileSystemService {

    Path createTempDirectory(String prefix);

    Path saveVideoFile(byte[] content, String filename, String directory);

    Path createZipFile(Path framesDirectory, String videoId, String outputDirectory);

    void deleteFile(Path filePath);

    void deleteDirectory(Path directoryPath);

    boolean fileExists(Path filePath);

    Path getFilePath(String directory, String filename);
}
