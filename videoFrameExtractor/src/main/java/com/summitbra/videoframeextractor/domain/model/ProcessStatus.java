package com.summitbra.videoframeextractor.domain.model;

public enum ProcessStatus {
    RECEIVED("RECEIVED"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    PROCESS_ERROR("PROCESS_ERROR");

    private final String value;

    ProcessStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
