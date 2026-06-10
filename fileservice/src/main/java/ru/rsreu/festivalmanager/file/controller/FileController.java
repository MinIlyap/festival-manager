package ru.rsreu.festivalmanager.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.file.entity.EntityType;
import ru.rsreu.festivalmanager.file.service.FileStorageService;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/avatar", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> uploadAvatar(
            @RequestBody byte[] fileBytes,
            @AuthenticationPrincipal Long userId) {

        fileService.uploadFile(fileBytes, EntityType.USER_AVATAR, userId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/avatar/{userId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        try {
            byte[] fileBytes = fileService.downloadFile(EntityType.USER_AVATAR, userId);
            return ResponseEntity.ok(fileBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(@AuthenticationPrincipal Long userId) {
        fileService.deleteFile(EntityType.USER_AVATAR, userId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping(value = "/{entityType}/{entityId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> uploadEntityImage(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId,
            @RequestBody byte[] fileBytes,
            @AuthenticationPrincipal Long companyId) {

        fileService.uploadFile(fileBytes, entityType, entityId, companyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{entityType}/{entityId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getEntityImage(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {

        try {
            byte[] fileBytes = fileService.downloadFile(entityType, entityId);
            return ResponseEntity.ok(fileBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("hasAnyAuthority('COMPANY', 'ADMIN')")
    @DeleteMapping("/{entityType}/{entityId}")
    public ResponseEntity<Void> deleteEntityImage(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId,
            @AuthenticationPrincipal Long companyId) {

        fileService.deleteFile(entityType, entityId, companyId);
        return ResponseEntity.noContent().build();
    }
}
