package com.fiap.videoframeextractor.infrastructure.adapter.out.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fiap.videoframeextractor.domain.exceptions.StorageException;
import com.fiap.videoframeextractor.domain.exceptions.VideoNotFoundException;
import com.fiap.videoframeextractor.domain.model.VideoMetadata;
import com.fiap.videoframeextractor.domain.ports.out.VideoStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter implements VideoStoragePort {

    private final AmazonS3 s3Client;

    @Value("${app.aws.s3.bucket-name:postech-fiap-bucket-videos}")
    private String bucketName;

    @Value("${app.aws.s3.videos-prefix:videos/}")
    private String videosPrefix;

    @Value("${app.aws.s3.frames-prefix:frames/}")
    private String framesPrefix;

    @Override
    public byte[] downloadVideo(String videoPath) {
        try {
            log.info("Downloading video from S3: bucket={}, key={}", bucketName, videoPath);

            S3Object s3Object = s3Client.getObject(bucketName, videoPath);
            byte[] content = s3Object.getObjectContent().readAllBytes();

            log.info("Successfully downloaded video: {} bytes", content.length);
            return content;

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new VideoNotFoundException("Video not found in S3: " + videoPath, e);
            }
            throw new StorageException("Failed to download video from S3: " + videoPath, e);
        } catch (IOException e) {
            throw new StorageException("Failed to read video content: " + videoPath, e);
        }
    }

    @Override
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
            throw new StorageException("Failed to upload frames ZIP to S3: " + videoId, e);
        }
    }

    @Override
    public boolean videoExists(String videoPath) {
        try {
            s3Client.getObjectMetadata(bucketName, videoPath);
            return true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw new StorageException("Failed to check video existence: " + videoPath, e);
        }
    }

    @Override
    public VideoMetadata getVideoMetadata(String videoPath) {
        try {
            ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, videoPath);

            return VideoMetadata.builder()
                .path(videoPath)
                .fileName(videoPath.substring(videoPath.lastIndexOf('/') + 1))
                .size(metadata.getContentLength())
                .contentType(metadata.getContentType())
                .build();

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new VideoNotFoundException("Video not found in S3: " + videoPath, e);
            }
            throw new StorageException("Failed to get video metadata: " + videoPath, e);
        }
    }
}
