package com.summitbra.videoframeextractor.application.port.out;

import com.summitbra.videoframeextractor.domain.model.VideoFile;

import java.nio.file.Path;

public interface VideoStoragePort {


    Path saveTemporaryFile(VideoFile videoFile);

    boolean removeTemporaryFile(Path filePath);

    Path createFramesZip(Path framesDirectory, String zipFileName);

    boolean zipFileExists(String zipFileName);

    Path getZipFilePath(String zipFileName);

    int cleanupOldFiles(int olderThanHours);
}
