package com.fiap.videoframeextractor.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class VideoStatusMessage {

    private String nmPersonEmail;
    private String idVideoSend;
    private String cdVideoStatus;
    private String nmVideo;
    private String nmPersonName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateTimeVideoProcessCompleted;
    private String nmVideoPathZip;
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

    public static VideoStatusMessage completed(String videoId, String userEmail, String fileName, String userName, String nmVideoPathZip) {
        return VideoStatusMessage.builder()
                .nmPersonEmail(userEmail)
                .idVideoSend(videoId)
                .cdVideoStatus("COMPLETED")
                .nmVideo(fileName)
                .nmPersonName(userName)
                .nmVideoPathZip(nmVideoPathZip)
                .dateTimeVideoProcessCompleted(LocalDateTime.now())
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
