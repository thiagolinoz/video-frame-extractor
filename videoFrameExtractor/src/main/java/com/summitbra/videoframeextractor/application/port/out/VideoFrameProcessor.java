package com.summitbra.videoframeextractor.application.port.out;

import com.summitbra.videoframeextractor.domain.model.VideoFile;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.domain.model.FrameExtractionResult;

public interface VideoFrameProcessor {

    FrameExtractionResult extractFrames(VideoFile videoFile,
                                       String videoId,
                                       VideoProcessingJob.ProcessingParameters parameters);
}
