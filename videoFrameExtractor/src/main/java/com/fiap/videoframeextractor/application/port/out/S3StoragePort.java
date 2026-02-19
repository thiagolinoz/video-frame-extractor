package com.fiap.videoframeextractor.application.port.out;

public interface S3StoragePort {

    byte[] downloadVideo(String videoId);

    String uploadFramesZip(String videoId, byte[] zipData);

    boolean videoExists(String videoId);

    VideoMetadata getVideoMetadata(String videoId);

    record VideoMetadata(
        String videoId,
        long size,
        String contentType,
        String lastModified
    ) {}
}
