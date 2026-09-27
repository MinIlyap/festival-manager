package ru.rsreu.festivalmanager.jwt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.rsreu.festivalmanager.jwt.security.InternalAuthFilter;
import ru.rsreu.festivalmanager.jwt.security.JwtFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Value("${internal.system-token:}")
    private String systemToken;

    @Value("${app.security.public-endpoints:}")
    private String[] publicEndpoints;

    private final Optional<OAuth2UserService<OAuth2UserRequest, OAuth2User>> oAuth2UserService;
    private final Optional<AuthenticationSuccessHandler> oAuth2SuccessHandler;
    private final Optional<AuthenticationFailureHandler> oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (systemToken != null && !systemToken.isEmpty()) {
            http.addFilterBefore(new InternalAuthFilter(systemToken), UsernamePasswordAuthenticationFilter.class);
        }

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    if (publicEndpoints != null && publicEndpoints.length > 0) {
                        auth.requestMatchers(publicEndpoints).permitAll();
                    }

                    auth.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        if (oAuth2UserService.isPresent() && oAuth2SuccessHandler.isPresent()) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService.get()))
                    .successHandler(oAuth2SuccessHandler.get())
                    .failureHandler(oAuth2FailureHandler.orElse(null))
            );
        }

        return http.build();
    }
}
