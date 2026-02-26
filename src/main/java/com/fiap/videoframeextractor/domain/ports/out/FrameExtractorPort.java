package com.fiap.videoframeextractor.domain.ports.out;

public interface FrameExtractorPort {
    byte[] extractFramesToZip(byte[] videoData, String fileName, double interval, int maxFrames);
}
