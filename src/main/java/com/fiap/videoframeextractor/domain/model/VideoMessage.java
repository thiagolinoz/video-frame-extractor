package com.fiap.videoframeextractor.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMessage {

    private String nmPersonEmail;
    private String idVideoSend;
    private String cdVideoStatus;
    private String nmVideo;
    private String nmVideoPathOrigin;
    private String nmVideoPathZip;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date dateTimeVideoCreated;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date dateTimeVideoProcessCompleted;
    private String nmPersonName;

    public boolean isValidForProcessing() {
        return nmPersonEmail != null &&
               idVideoSend != null &&
               nmVideo != null &&
               nmVideoPathOrigin != null;
    }

//    public String getVideoId() {
//        return idVideoSend;
//    }
//
//    public String getFileName() {
//        return nmVideo;
//    }
//
//    public String getUserEmail() {
//        return nmPersonEmail;
//    }
//
//    public String getVideoPath() {
//        return nmVideoPathOrigin;
//    }
//
//    public String getUserName() {
//        return nmPersonName;
//    }
}
