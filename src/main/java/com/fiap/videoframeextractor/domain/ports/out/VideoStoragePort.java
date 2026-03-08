package com.fiap.videoframeextractor.domain.ports.out;

import com.fiap.videoframeextractor.domain.model.VideoMetadata;

public interface VideoStoragePort {
    boolean videoExists(String videoPath);
    byte[] downloadVideo(String videoPath);
    String uploadFramesZip(String videoId, byte[] framesZip);
    VideoMetadata getVideoMetadata(String videoPath);
}
