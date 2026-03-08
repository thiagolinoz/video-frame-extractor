package com.fiap.videoframeextractor.domain.enums;

public enum FrameFormatEnum {
    JPEG("jpeg"),
    PNG("png"),
    BMP("bmp");

    private final String format;

    FrameFormatEnum(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
