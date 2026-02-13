package com.summitbra.videoframeextractor.application.port.out;

import com.summitbra.videoframeextractor.domain.model.VideoFile;

public interface VideoFileValidationPort {


    boolean isValidVideoFile(VideoFile videoFile);

    boolean isSupportedFormat(String contentType);

    VideoMetadata getVideoMetadata(VideoFile videoFile);

    class VideoValidationException extends RuntimeException {
        public VideoValidationException(String message) {
            super(message);
        }

        public VideoValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    record VideoMetadata(
        double duration,
        int width,
        int height,
        String codec,
        double frameRate
    ) {}
}
