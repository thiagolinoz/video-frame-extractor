package com.fiap.videoframeextractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMessage {

    private String nmPersonEmail;
    private String idVideoSend;
    private String cdVideoStatus;
    private String nmVideo;
    private String nmVideoPathOrigin;
    private String nmVideoPathZip;
    private Date dateTimeVideoCreated;
    private Date dateTimeVideoProcessCompleted;
    private String nmPersonName;

    public boolean isValidForProcessing() {
        return nmPersonEmail != null &&
               idVideoSend != null &&
               nmVideo != null &&
               nmVideoPathOrigin != null;
    }

    public String getVideoId() {
        return idVideoSend;
    }

    public String getFileName() {
        return nmVideo;
    }

    public String getUserEmail() {
        return nmPersonEmail;
    }

    public String getVideoPath() {
        return nmVideoPathOrigin;
    }

    public String getUserName() {
        return nmPersonName;
    }
}
