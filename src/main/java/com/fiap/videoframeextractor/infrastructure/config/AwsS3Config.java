package com.fiap.videoframeextractor.infrastructure.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "app.aws.s3.enabled", havingValue = "true", matchIfMissing = true)
public class AwsS3Config {

    @Value("${app.aws.region:us-east-1}")
    private String region;

    @Value("${app.aws.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${app.aws.s3.path-style-access:false}")
    private boolean pathStyleAccessEnabled;

    @Bean public AmazonS3 amazonS3Client() {
        //log.info("Initializing AWS S3 client for region: {}", region);

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(pathStyleAccessEnabled);

        if (s3Endpoint != null && !s3Endpoint.trim().isEmpty()) {
            //log.info("Using custom S3 endpoint: {}", s3Endpoint);
            builder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(s3Endpoint, region)
            );
        } else {
            builder.withRegion(region);
        }

        AmazonS3 s3Client = builder.build();

        //log.info("AWS S3 client initialized successfully");
        return s3Client;
    }

}
