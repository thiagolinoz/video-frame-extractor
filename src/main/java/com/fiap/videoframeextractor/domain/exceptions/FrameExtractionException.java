package com.fiap.videoframeextractor.domain.exceptions;

public class FrameExtractionException extends RuntimeException {
    public FrameExtractionException(String message) {
        super(message);
    }

    public FrameExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
