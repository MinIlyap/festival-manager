package ru.rsreu.festivalmanager.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.auth.entity.UserSocialAccount;
import ru.rsreu.festivalmanager.auth.repository.AuthCredentialRepository;
import ru.rsreu.festivalmanager.auth.repository.UserSocialAccountRepository;
import ru.rsreu.festivalmanager.auth.service.AuthService;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserSocialAccountRepository socialRepository;
    private final AuthCredentialRepository credentialRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String externalId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("default_email");
        if (email == null) {
            email = (String) attributes.get("email");
        }
        String displayName = (String) attributes.get("display_name");

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        if (currentAuth != null && currentAuth.getPrincipal() instanceof Long id) {
            currentUserId = id;
        }

        Optional<UserSocialAccount> socialAccountOpt = socialRepository.findByProviderAndExternalId(registrationId, externalId);
        Long finalUserId;

        if (currentUserId != null) {
            if (socialAccountOpt.isPresent()) {
                if (!socialAccountOpt.get().getUserId().equals(currentUserId)) {
                    throw new OAuth2AuthenticationException("Этот аккаунт Яндекс уже привязан к другому профилю.");
                }
            } else {
                UserSocialAccount socialAccount = UserSocialAccount.builder()
                        .userId(currentUserId)
                        .provider(registrationId)
                        .externalId(externalId)
                        .email(email)
                        .build();
                socialRepository.save(socialAccount);
            }
            finalUserId = currentUserId;
        } else {
            if (socialAccountOpt.isPresent()) {
                finalUserId = socialAccountOpt.get().getUserId();
            } else {
                if (email != null && credentialRepository.findByEmail(email).isPresent()) {
                    throw new OAuth2AuthenticationException("Email занят. Войдите по паролю и привяжите аккаунт в настройках.");
                }
                finalUserId = authService.registerExternalUser(email, displayName);

                UserSocialAccount socialAccount = UserSocialAccount.builder()
                        .userId(finalUserId)
                        .provider(registrationId)
                        .externalId(externalId)
                        .email(email)
                        .build();
                socialRepository.save(socialAccount);
            }
        }

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                Map.of("userId", finalUserId, "email", email != null ? email : "", "id", externalId),
                "userId"
        );
    }
}
