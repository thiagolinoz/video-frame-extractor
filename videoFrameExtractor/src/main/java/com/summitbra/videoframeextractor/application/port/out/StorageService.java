package com.summitbra.videoframeextractor.application.port.out;

import java.nio.file.Path;

public interface StorageService {
    byte[] downloadFile(String bucketName, String key);
    void uploadFile(String bucketName, String key, byte[] content);
    void uploadFile(String bucketName, String key, Path filePath);
}
