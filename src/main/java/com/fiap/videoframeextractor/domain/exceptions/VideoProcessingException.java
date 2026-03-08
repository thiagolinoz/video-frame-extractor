package com.fiap.videoframeextractor.domain.exceptions;

public class VideoProcessingException extends RuntimeException {
    public VideoProcessingException(String message) {
        super(message);
    }

    public VideoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
