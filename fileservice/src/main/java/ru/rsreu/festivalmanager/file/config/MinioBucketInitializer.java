package ru.rsreu.festivalmanager.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.file.entity.Bucket;
import ru.rsreu.festivalmanager.file.repository.BucketRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer implements ApplicationRunner {
    private final MinioClient minioClient;
    private final BucketRepository bucketRepository;

    private static final List<String> REQUIRED_BUCKETS = List.of(
            "avatars",
            "festivals",
            "events",
            "rewards",
            "companies"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String bucketName : REQUIRED_BUCKETS) {
            ensureMinioBucket(bucketName);
            ensureDbRecord(bucketName);
        }
    }

    private void ensureMinioBucket(String bucketName) {
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
            log.error("Failed to ensure MinIO bucket '{}': {}", bucketName, e.getMessage(), e);
        }
    }

    private void ensureDbRecord(String bucketName) {
        if (bucketRepository.findByName(bucketName).isEmpty()) {
            bucketRepository.save(Bucket.builder().name(bucketName).build());
            log.info("DB bucket record created: {}", bucketName);
        }
    }
}
