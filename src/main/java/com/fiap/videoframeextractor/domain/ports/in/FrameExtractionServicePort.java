package com.fiap.videoframeextractor.domain.ports.in;

import com.fiap.videoframeextractor.domain.model.VideoMessage;

public interface FrameExtractionServicePort {
    String processVideo(VideoMessage videoMessage);
    void processVideoMessage(String message);
}
