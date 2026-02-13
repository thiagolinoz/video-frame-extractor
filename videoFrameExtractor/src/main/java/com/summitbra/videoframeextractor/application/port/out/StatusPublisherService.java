package com.summitbra.videoframeextractor.application.port.out;

import com.summitbra.videoframeextractor.domain.model.ProcessStatusMessage;
import com.summitbra.videoframeextractor.domain.model.ProcessStatus;
import com.summitbra.videoframeextractor.domain.model.VideoMessage;

public interface StatusPublisherService {
    void sendProcessingStatus(VideoMessage videoMessage, ProcessStatus status, String message);
    void sendProcessingStatus(ProcessStatusMessage statusMessage);
}
