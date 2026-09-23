package ru.rsreu.festivalmanager.file.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.file.client.EventServiceClient;
import ru.rsreu.festivalmanager.file.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.file.entity.Bucket;
import ru.rsreu.festivalmanager.file.entity.EntityType;
import ru.rsreu.festivalmanager.file.entity.UserFile;
import ru.rsreu.festivalmanager.file.repository.BucketRepository;
import ru.rsreu.festivalmanager.file.repository.UserFileRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final MinioClient minioClient;
    private final UserFileRepository fileRepository;
    private final BucketRepository bucketRepository;
    private final FestivalServiceClient festivalClient;
    private final EventServiceClient eventClient;
    private final Tika tika = new Tika();

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }

    @Transactional
    public void uploadFile(byte[] fileBytes, EntityType type, Long entityId, Long creatorId) {
        if (fileBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Размер файла превышает 5 МБ");
        }

        String mimeType = tika.detect(fileBytes);
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Разрешена загрузка только изображений");
        }

        checkPermissions(type, entityId, creatorId);

        fileRepository.findByEntityTypeAndEntityId(type, entityId)
                .ifPresent(this::deleteImageInternal);

        String bucketName = switch (type) {
            case USER_AVATAR, COMPANY_LOGO -> "avatars";
            case FESTIVAL_LOCATION -> "festivals";
            case FESTIVAL_EVENT, FESTIVAL_STAND, PRODUCT_INSTANCE -> "events";
            case EVENT_REWARD -> "rewards";
            default -> throw new IllegalArgumentException("Неизвестный тип сущности");
        };

        Bucket bucket = bucketRepository.findByName(bucketName)
                .orElseThrow(() -> new RuntimeException("Бакет " + bucketName + " не найден в БД"));

        String objectName = UUID.randomUUID() + ".img";

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket.getName())
                            .object(objectName)
                            .stream(inputStream, fileBytes.length, -1)
                            .contentType(mimeType)
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка загрузки файла в MinIO", e);
            throw new RuntimeException("Не удалось сохранить файл");
        }

        UserFile userFile = UserFile.builder()
                .entityType(type)
                .entityId(entityId)
                .bucket(bucket)
                .objectName(objectName)
                .contentType(mimeType)
                .fileSize((long) fileBytes.length)
                .creatorId(creatorId)
                .build();

        fileRepository.save(userFile);
    }

    @Transactional(readOnly = true)
    public byte[] downloadFile(EntityType type, Long entityId) {
        UserFile file = fileRepository.findByEntityTypeAndEntityId(type, entityId)
                .orElseThrow(() -> new RuntimeException("Изображение не найдено"));

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(file.getBucket().getName())
                        .object(file.getObjectName())
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Ошибка скачивания файла из MinIO", e);
            throw new RuntimeException("Не удалось прочитать файл");
        }
    }

    @Transactional
    public void deleteFile(EntityType type, Long entityId, Long creatorId) {
        UserFile file = fileRepository.findByEntityTypeAndEntityId(type, entityId)
                .orElseThrow(() -> new RuntimeException("Изображение не найдено"));

        checkPermissions(type, entityId, creatorId);
        deleteImageInternal(file);
    }

    private void deleteImageInternal(UserFile file) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(file.getBucket().getName())
                            .object(file.getObjectName())
                            .build()
            );
            fileRepository.delete(file);
        } catch (Exception e) {
            log.error("Ошибка удаления файла из MinIO", e);
            throw new RuntimeException("Не удалось удалить файл");
        }
    }

    private void checkPermissions(EntityType type, Long entityId, Long creatorId) {
        if (type == EntityType.USER_AVATAR || type == EntityType.COMPANY_LOGO) {
            if (!entityId.equals(creatorId)) {
                throw new SecurityException("Вы не можете изменять чужой файл");
            }
            return;
        }

        boolean hasAccess = switch (type) {
            case FESTIVAL_LOCATION -> festivalClient.checkAccess(getSystemToken(), type.name(), entityId, creatorId);
            case FESTIVAL_EVENT, FESTIVAL_STAND, EVENT_REWARD, PRODUCT_INSTANCE -> eventClient.checkAccess(getSystemToken(), type.name(), entityId, creatorId);
            default -> false;
        };

        if (!hasAccess) {
            throw new SecurityException("У вас нет прав компании-организатора для этой сущности");
        }
    }
}
