package com.fiap.videoframeextractor.infrastructure.adapter.out.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter {

    private final AmazonS3 s3Client;

    @Value("${app.aws.s3.bucket-name:postech-fiap-bucket-videos-fase5}")
    private String bucketName;

    @Value("${app.aws.s3.videos-prefix:videos/}")
    private String videosPrefix;

    @Value("${app.aws.s3.frames-prefix:frames/}")
    private String framesPrefix;

    public byte[] downloadVideo(String videoId) {
        String videoKey = videosPrefix + videoId;

        try {
            log.info("Downloading video from S3: bucket={}, key={}", bucketName, videoKey);

            S3Object s3Object = s3Client.getObject(bucketName, videoKey);
            byte[] content = s3Object.getObjectContent().readAllBytes();

            log.info("Successfully downloaded video: {} bytes", content.length);
            return content;

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new VideoNotFoundException("Video not found in S3: " + videoId, e);
            }
            throw new S3OperationException("Failed to download video from S3: " + videoId, e);
        } catch (IOException e) {
            throw new S3OperationException("Failed to read video content: " + videoId, e);
        }
    }

    public String uploadFramesZip(String videoId, byte[] zipData) {
        String frameKey = framesPrefix + videoId + ".zip";

        try {
            log.info("Uploading frames ZIP to S3: bucket={}, key={}, size={} bytes",
                bucketName, frameKey, zipData.length);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(zipData.length);
            metadata.setContentType("application/zip");
            metadata.addUserMetadata("video-id", videoId);
            metadata.addUserMetadata("upload-time", String.valueOf(System.currentTimeMillis()));

            PutObjectRequest putRequest = new PutObjectRequest(
                bucketName,
                frameKey,
                new ByteArrayInputStream(zipData),
                metadata
            );

            PutObjectResult result = s3Client.putObject(putRequest);

            log.info("Successfully uploaded frames ZIP: key={}, etag={}",
                frameKey, result != null ? result.getETag() : "no-etag");
            return frameKey;

        } catch (AmazonS3Exception e) {
            throw new S3OperationException("Failed to upload frames ZIP to S3: " + videoId, e);
        }
    }

    public boolean videoExists(String videoId) {
        String videoKey = videosPrefix + videoId;

        try {
            s3Client.getObjectMetadata(bucketName, videoKey);
            return true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw new S3OperationException("Failed to check video existence: " + videoId, e);
        }
    }

    public VideoMetadata getVideoMetadata(String videoId) {
        String videoKey = videosPrefix + videoId;

        try {
            ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, videoKey);

            return new VideoMetadata(
                videoId,
                metadata.getContentLength(),
                metadata.getContentType(),
                metadata.getLastModified().toString()
            );

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new VideoNotFoundException("Video not found in S3: " + videoId, e);
            }
            throw new S3OperationException("Failed to get video metadata: " + videoId, e);
        }
    }


    public static class VideoMetadata {
        private final String videoId;
        private final long size;
        private final String contentType;
        private final String lastModified;

        public VideoMetadata(String videoId, long size, String contentType, String lastModified) {
            this.videoId = videoId;
            this.size = size;
            this.contentType = contentType;
            this.lastModified = lastModified;
        }

        public String getVideoId() { return videoId; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public String getLastModified() { return lastModified; }
    }

    public static class S3OperationException extends RuntimeException {
        public S3OperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class VideoNotFoundException extends RuntimeException {
        public VideoNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
