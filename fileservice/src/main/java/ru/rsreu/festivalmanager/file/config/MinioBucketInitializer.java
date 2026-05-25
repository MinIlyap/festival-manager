package ru.rsreu.festivalmanager.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;

    private static final List<String> REQUIRED_BUCKETS = List.of(
            "avatars",
            "festivals",
            "events",
            "rewards"
    );

    @Override
    public void run(ApplicationArguments args) {
        for (String bucketName : REQUIRED_BUCKETS) {
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );
                if (!exists) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucketName).build()
                    );
                    log.info("MinIO bucket created: {}", bucketName);
                } else {
                    log.debug("MinIO bucket already exists: {}", bucketName);
                }
            } catch (Exception e) {
                log.error("Failed to ensure MinIO bucket '{}' exists: {}", bucketName, e.getMessage(), e);
            }
        }
    }
}
