package com.summitbra.videoframeextractor.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VideoFile {
    String filename;
    byte[] content;
    long size;
    String contentType;

    public String getExtension() {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".mp4";
    }
}
