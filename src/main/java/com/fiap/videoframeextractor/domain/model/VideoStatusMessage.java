package com.fiap.videoframeextractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStatusMessage {

    private String nmPersonEmail;
    private String idVideoSend;
    private String cdVideoStatus;
    private String nmVideo;
    private String nmPersonName;
    private String errorMessage;
//    private LocalDateTime timestamp;

    public static VideoStatusMessage processing(String videoId, String userEmail, String fileName, String userName) {
        return VideoStatusMessage.builder()
            .nmPersonEmail(userEmail)
            .idVideoSend(videoId)
            .cdVideoStatus("PROCESSING")
            .nmVideo(fileName)
            .nmPersonName(userName)
//            .timestamp(LocalDateTime.now())
            .build();
    }

    public static VideoStatusMessage completed(String videoId, String userEmail, String fileName, String userName) {
        return VideoStatusMessage.builder()
            .nmPersonEmail(userEmail)
            .idVideoSend(videoId)
            .cdVideoStatus("COMPLETED")
            .nmVideo(fileName)
            .nmPersonName(userName)
//            .timestamp(LocalDateTime.now())
            .build();
    }

    public static VideoStatusMessage error(String videoId, String userEmail, String fileName, String userName) {
        return VideoStatusMessage.builder()
            .nmPersonEmail(userEmail)
            .idVideoSend(videoId)
            .cdVideoStatus("PROCESS_ERROR")
            .nmVideo(fileName)
            .nmPersonName(userName)
//            .timestamp(LocalDateTime.now())
            .build();
    }

//    public String getVideoId() { return idVideoSend; }
//    public String getUserEmail() { return nmPersonEmail; }
//    public String getFileName() { return nmVideo; }
//    public String getUserName() { return nmPersonName; }
//    public String getStatus() { return cdVideoStatus; }
}
