package com.summitbra.videoframeextractor.application.port.in;

import com.summitbra.videoframeextractor.domain.model.VideoFile;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;

public interface ProcessVideoFramesUseCase {

    VideoProcessingJob processVideo(VideoFile videoFile, ProcessVideoCommand command);

    record ProcessVideoCommand(
        double intervalSeconds,
        int maxFrames,
        String outputFormat
    ) {
        public ProcessVideoCommand {
            if (intervalSeconds <= 0) {
                throw new IllegalArgumentException("Interval seconds must be positive");
            }
            if (maxFrames <= 0) {
                throw new IllegalArgumentException("Max frames must be positive");
            }
            if (outputFormat == null || outputFormat.trim().isEmpty()) {
                outputFormat = "PNG";
            }
        }
    }
}
