package ru.rsreu.festivalmanager.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.festivalmanager.auth.dto.*;
import ru.rsreu.festivalmanager.auth.entity.AuthCredential;
import ru.rsreu.festivalmanager.auth.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/user")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegistrationRequest request) {
        authService.submitCompanyApplication(request);
        return ResponseEntity.accepted().body("Заявка компании отправлена на модерацию");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<AuthCredential> getCredentialByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.getCredentialByUserId(userId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<AuthCredential> getUserByEmail(@RequestParam("email") String email) {
        AuthCredential credential = authService.getUserByEmail(email);
        return ResponseEntity.ok(credential);
    }

    @PutMapping("/roles/curator")
    @PreAuthorize("hasRole('INTERNAL')")
    public void assignCuratorRoles(
            @RequestBody List<Long> userIds) {

        authService.assignCuratorRoles(userIds);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthCredential> getMe(
            @AuthenticationPrincipal Long id,
            Authentication authentication) {

        AuthCredential info = authService.getCurrentUserInfo(id, extractRole(authentication));
        return ResponseEntity.ok(info);
    }

    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request,
            @AuthenticationPrincipal Long id,
            Authentication authentication) {

        authService.changeEmail(id, extractRole(authentication), request.getNewEmail());
        return ResponseEntity.ok("Email успешно изменен");
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Long id,
            Authentication authentication) {

        authService.changePassword(id, extractRole(authentication), request);
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    @DeleteMapping("/{provider}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Void> unlinkSocialAccount(
            @AuthenticationPrincipal Long userId,
            @PathVariable String provider) {

        authService.unlinkSocialAccount(userId, provider);
        return ResponseEntity.ok().build();
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }
}
