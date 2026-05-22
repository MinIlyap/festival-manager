package ru.rsreu.festivalmanager.event.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.dto.QrRegistrationResponse;
import ru.rsreu.festivalmanager.event.dto.VerifyQrRegistrationResponse;
import ru.rsreu.festivalmanager.event.repository.CommandMemberRepository;
import ru.rsreu.festivalmanager.event.repository.FestivalEventRepository;
import ru.rsreu.festivalmanager.event.repository.UserEventRegistrationRepository;
import ru.rsreu.festivalmanager.jwt.security.QrJwtProvider;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventQrService {

    private final UserEventRegistrationRepository registrationRepository;
    private final CommandMemberRepository commandMemberRepository;
    private final FestivalEventRepository festivalEventRepository;
    private final QrJwtProvider qrJwtProvider;

    public QrRegistrationResponse generateEventTicket(Long festivalEventId, Long userId) {
        if (!festivalEventRepository.existsById(festivalEventId)) {
            throw new IllegalArgumentException("Мероприятие не найдено");
        }
        boolean isRegisteredIndividually = registrationRepository
                .existsByUserEventRegistrationId_FestivalEvent_IdAndUserEventRegistrationId_UserId(festivalEventId, userId);
        boolean isRegisteredInCommand = commandMemberRepository
                .existsByCommandMemberId_UserIdAndCommandMemberId_Command_FestivalEvent_id(userId, festivalEventId);

        if (!isRegisteredIndividually && !isRegisteredInCommand) {
            log.warn("Пользователь {} пытался сгенерировать билет на мероприятие {}, не будучи зарегистрированным", userId, festivalEventId);
            throw new SecurityException("Вы не зарегистрированы на это мероприятие");
        }

        Map<String, Object> claims = Map.of(
                "fest_evt_id", festivalEventId
        );

        String token = qrJwtProvider.generateQrToken(claims, userId);
        return new QrRegistrationResponse(token);
    }

    public VerifyQrRegistrationResponse verifyEventTicket(Long festivalEventId, String token, Long companyId) {
        boolean isOwner = festivalEventRepository.findById(festivalEventId)
                .map(event -> event.getEventInstance().getCompanyId().equals(companyId))
                .orElse(false);

        if (!isOwner) {
            log.error("Компания {} пыталась проверить билеты чужого мероприятия {}", companyId, festivalEventId);
            throw new SecurityException("У вашей компании нет прав на проверку билетов этого мероприятия");
        }

        try {
            Claims claims = qrJwtProvider.extractClaims(token);
            Long tokenFestEvtId = claims.get("fest_evt_id", Long.class);
            Long tokenUserId = Long.valueOf(claims.getSubject());

            if (!festivalEventId.equals(tokenFestEvtId)) {
                return VerifyQrRegistrationResponse.builder()
                        .status(VerifyQrRegistrationResponse.VerificationStatus.LOCATION_MISMATCH)
                        .targetId(tokenUserId)
                        .locationId(tokenFestEvtId)
                        .build();
            }

            return VerifyQrRegistrationResponse.builder()
                    .status(VerifyQrRegistrationResponse.VerificationStatus.SUCCESS)
                    .targetId(tokenUserId)
                    .locationId(tokenFestEvtId)
                    .build();

        } catch (ExpiredJwtException e) {
            return VerifyQrRegistrationResponse.builder()
                    .status(VerifyQrRegistrationResponse.VerificationStatus.EXPIRED)
                    .targetId(Long.valueOf(e.getClaims().getSubject()))
                    .build();
        } catch (Exception e) {
            return VerifyQrRegistrationResponse.builder().status(VerifyQrRegistrationResponse.VerificationStatus.INVALID).build();
        }
    }
}
