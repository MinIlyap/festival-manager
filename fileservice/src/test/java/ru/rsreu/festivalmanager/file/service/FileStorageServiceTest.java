package ru.rsreu.festivalmanager.file.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rsreu.festivalmanager.file.client.EventServiceClient;
import ru.rsreu.festivalmanager.file.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.file.entity.Bucket;
import ru.rsreu.festivalmanager.file.entity.EntityType;
import ru.rsreu.festivalmanager.file.entity.UserFile;
import ru.rsreu.festivalmanager.file.repository.BucketRepository;
import ru.rsreu.festivalmanager.file.repository.UserFileRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileStorageServiceTest {

    private static final String BEARER = "Bearer system-token";

    @Mock private MinioClient minioClient;
    @Mock private UserFileRepository fileRepository;
    @Mock private BucketRepository bucketRepository;
    @Mock private FestivalServiceClient festivalClient;
    @Mock private EventServiceClient eventClient;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService(minioClient, fileRepository, bucketRepository, festivalClient, eventClient);
        ReflectionTestUtils.setField(service, "systemToken", "system-token");
    }

    private static byte[] pngBytes() {
        byte[] png = new byte[64];
        byte[] signature = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        System.arraycopy(signature, 0, png, 0, signature.length);
        return png;
    }

    private void bucketExists(String name) {
        when(bucketRepository.findByName(name))
                .thenReturn(Optional.of(Bucket.builder().name(name).build()));
    }

    private static UserFile storedFile(String bucketName, String objectName) {
        return UserFile.builder()
                .entityType(EntityType.USER_AVATAR)
                .entityId(42L)
                .bucket(Bucket.builder().name(bucketName).build())
                .objectName(objectName)
                .contentType("image/png")
                .fileSize(64L)
                .creatorId(42L)
                .build();
    }

    @Nested
    @DisplayName("Проверка самого файла")
    class FileValidation {

        @Test
        @DisplayName("файл больше 5 МБ отклоняется")
        void rejectsOversizedFile() {
            byte[] tooBig = new byte[5 * 1024 * 1024 + 1];

            assertThatThrownBy(() -> service.uploadFile(tooBig, EntityType.USER_AVATAR, 42L, 42L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("5 МБ");

            verifyNoInteractions(minioClient, fileRepository);
        }

        @Test
        @DisplayName("не изображение отклоняется, даже если права есть")
        void rejectsNonImage() {
            byte[] text = "это обычный текст, а не картинка".getBytes();

            assertThatThrownBy(() -> service.uploadFile(text, EntityType.USER_AVATAR, 42L, 42L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("только изображений");

            verifyNoInteractions(minioClient);
            verify(fileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Права на загрузку")
    class Permissions {

        @Test
        @DisplayName("нельзя заменить аватар другого пользователя")
        void cannotTouchSomeoneElsesAvatar() {
            assertThatThrownBy(() -> service.uploadFile(pngBytes(), EntityType.USER_AVATAR, 42L, 99L))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("чужой файл");

            verifyNoInteractions(minioClient);
            verify(fileRepository, never()).save(any());
        }

        @Test
        @DisplayName("нельзя заменить логотип другой компании")
        void cannotTouchSomeoneElsesCompanyLogo() {
            assertThatThrownBy(() -> service.uploadFile(pngBytes(), EntityType.COMPANY_LOGO, 5L, 6L))
                    .isInstanceOf(SecurityException.class);

            verifyNoInteractions(minioClient);
        }

        @Test
        @DisplayName("права на фестиваль спрашиваются у festivalservice")
        void delegatesFestivalAccessCheck() {
            when(festivalClient.checkAccess(BEARER, "FESTIVAL_LOCATION", 11L, 5L)).thenReturn(false);

            assertThatThrownBy(() -> service.uploadFile(pngBytes(), EntityType.FESTIVAL_LOCATION, 11L, 5L))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("компании-организатора");

            verify(festivalClient).checkAccess(BEARER, "FESTIVAL_LOCATION", 11L, 5L);
            verifyNoInteractions(eventClient);
        }

        @Test
        @DisplayName("права на мероприятие спрашиваются у eventservice")
        void delegatesEventAccessCheck() {
            when(eventClient.checkAccess(BEARER, "FESTIVAL_EVENT", 3L, 5L)).thenReturn(false);

            assertThatThrownBy(() -> service.uploadFile(pngBytes(), EntityType.FESTIVAL_EVENT, 3L, 5L))
                    .isInstanceOf(SecurityException.class);

            verify(eventClient).checkAccess(BEARER, "FESTIVAL_EVENT", 3L, 5L);
            verifyNoInteractions(festivalClient);
        }

        @Test
        @DisplayName("удалять чужой файл тоже нельзя")
        void cannotDeleteSomeoneElsesFile() {
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.USER_AVATAR, 42L))
                    .thenReturn(Optional.of(storedFile("avatars", "old.img")));

            assertThatThrownBy(() -> service.deleteFile(EntityType.USER_AVATAR, 42L, 99L))
                    .isInstanceOf(SecurityException.class);

            verifyNoInteractions(minioClient);
            verify(fileRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Загрузка")
    class Upload {

        @Test
        @DisplayName("аватар кладётся в бакет avatars с определённым mime-типом")
        void storesAvatarInAvatarsBucket() throws Exception {
            bucketExists("avatars");
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.USER_AVATAR, 42L))
                    .thenReturn(Optional.empty());

            service.uploadFile(pngBytes(), EntityType.USER_AVATAR, 42L, 42L);

            ArgumentCaptor<PutObjectArgs> args = ArgumentCaptor.forClass(PutObjectArgs.class);
            verify(minioClient).putObject(args.capture());
            assertThat(args.getValue().bucket()).isEqualTo("avatars");
            assertThat(args.getValue().contentType()).isEqualTo("image/png");

            ArgumentCaptor<UserFile> saved = ArgumentCaptor.forClass(UserFile.class);
            verify(fileRepository).save(saved.capture());
            assertThat(saved.getValue().getEntityId()).isEqualTo(42L);
            assertThat(saved.getValue().getCreatorId()).isEqualTo(42L);
            assertThat(saved.getValue().getContentType()).isEqualTo("image/png");
            assertThat(saved.getValue().getObjectName()).endsWith(".img");
        }

        @Test
        @DisplayName("награда кладётся в бакет rewards")
        void storesRewardInRewardsBucket() throws Exception {
            when(eventClient.checkAccess(anyString(), eq("EVENT_REWARD"), anyLong(), anyLong())).thenReturn(true);
            bucketExists("rewards");
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.EVENT_REWARD, 8L))
                    .thenReturn(Optional.empty());

            service.uploadFile(pngBytes(), EntityType.EVENT_REWARD, 8L, 5L);

            ArgumentCaptor<PutObjectArgs> args = ArgumentCaptor.forClass(PutObjectArgs.class);
            verify(minioClient).putObject(args.capture());
            assertThat(args.getValue().bucket()).isEqualTo("rewards");
        }

        @Test
        @DisplayName("повторная загрузка удаляет прежний объект, а не копит мусор")
        void replacesPreviousFile() throws Exception {
            bucketExists("avatars");
            UserFile previous = storedFile("avatars", "previous.img");
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.USER_AVATAR, 42L))
                    .thenReturn(Optional.of(previous));

            service.uploadFile(pngBytes(), EntityType.USER_AVATAR, 42L, 42L);

            ArgumentCaptor<RemoveObjectArgs> removed = ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(removed.capture());
            assertThat(removed.getValue().object()).isEqualTo("previous.img");
            verify(fileRepository).delete(previous);
            verify(fileRepository).save(any(UserFile.class));
        }

        @Test
        @DisplayName("если бакета нет в БД, файл в MinIO не уходит")
        void failsWhenBucketIsMissing() throws Exception {
            when(bucketRepository.findByName("avatars")).thenReturn(Optional.empty());
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.USER_AVATAR, 42L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadFile(pngBytes(), EntityType.USER_AVATAR, 42L, 42L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Бакет");

            verify(minioClient, never()).putObject(any());
            verify(fileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Удаление")
    class Delete {

        @Test
        @DisplayName("свой файл удаляется и из MinIO, и из БД")
        void deletesOwnFile() throws Exception {
            UserFile file = storedFile("avatars", "mine.img");
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.USER_AVATAR, 42L))
                    .thenReturn(Optional.of(file));

            service.deleteFile(EntityType.USER_AVATAR, 42L, 42L);

            ArgumentCaptor<RemoveObjectArgs> removed = ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(removed.capture());
            assertThat(removed.getValue().bucket()).isEqualTo("avatars");
            assertThat(removed.getValue().object()).isEqualTo("mine.img");
            verify(fileRepository).delete(file);
        }

        @Test
        @DisplayName("отсутствующий файл — ошибка")
        void failsWhenFileIsMissing() {
            when(fileRepository.findByEntityTypeAndEntityId(EntityType.USER_AVATAR, 42L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteFile(EntityType.USER_AVATAR, 42L, 42L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("не найдено");
        }
    }
}
