package com.summitbra.videoframeextractor.domain.model;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

@Value
@Builder
public class FrameExtractionResult {
    int framesExtracted;
    long processingTimeMs;
    Path zipPath;
    String zipFilename;
    String downloadUrl;
}
