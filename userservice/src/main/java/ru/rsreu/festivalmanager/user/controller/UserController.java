package ru.rsreu.festivalmanager.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.user.dto.*;
import ru.rsreu.festivalmanager.user.service.UserService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Long> createUser(@Valid @RequestBody UserCreationDto dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/nickname-exists")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(userService.isNicknameTaken(nickname));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'CURATOR', 'ADMIN')")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(userService.getMe(id));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'CURATOR', 'ADMIN')")
    public ResponseEntity<String> updateMe(
            @AuthenticationPrincipal Long id,
            @Valid @RequestBody UserInfoUpdateRequest request) {

        userService.updateUser(id, request);
        return ResponseEntity.ok("Данные успешно обновлены");
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'CURATOR', 'ADMIN')")
    public ResponseEntity<String> patchMe(
            @AuthenticationPrincipal Long id,
            @Valid @RequestBody UserInfoPatchRequest request) {

        userService.patchUser(id, request);
        return ResponseEntity.ok("Данные успешно частично обновлены");
    }

    @GetMapping("/me/blacklist-check")
    @PreAuthorize("hasAnyRole('USER', 'CURATOR', 'ADMIN')")
    public ResponseEntity<BlacklistStatusResponse> getMyBlacklistStatus(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(userService.getMyBlacklistStatus(id));
    }

    @PostMapping("/blacklist/{userId}")
    @PreAuthorize("hasRole('CURATOR')")
    public ResponseEntity<String> addToBlackList(
            @AuthenticationPrincipal Long curatorId,
            @PathVariable Long userId,
            @Valid @RequestBody BlackListAddRequest request) {

        userService.addUserToBlackList(curatorId, userId, request);
        return ResponseEntity.ok("Пользователь успешно добавлен в черный список");
    }

    @GetMapping("/blacklist/check/{userId}")
    @PreAuthorize("hasAnyRole('INTERNAL', 'CURATOR', 'ADMIN')")
    public ResponseEntity<Boolean> checkBlackListStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.checkUserBlackListStatus(userId));
    }

    @GetMapping("/blacklist/status/{userId}")
    @PreAuthorize("hasAnyRole('INTERNAL', 'CURATOR', 'ADMIN')")
    public ResponseEntity<BlacklistStatusResponse> getBlackListStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getMyBlacklistStatus(userId));
    }

    @GetMapping("/users/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.searchUsersByNickname(nickname));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserPublicInfo(id));
    }
}
