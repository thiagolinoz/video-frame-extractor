package com.summitbra.videoframeextractor.application.port.in;

public interface CleanupVideoProcessingUseCase {

    void cleanupOldProcessings(int daysOld);
}
