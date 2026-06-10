package ru.rsreu.festivalmanager.festival.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.festival.dto.ticket.QrTicketResponse;
import ru.rsreu.festivalmanager.festival.dto.ticket.VerifyQrTicketResponse;
import ru.rsreu.festivalmanager.festival.entity.ticket.Ticket;
import ru.rsreu.festivalmanager.festival.repository.FestivalCuratorRepository;
import ru.rsreu.festivalmanager.festival.repository.TicketRepository;
import ru.rsreu.festivalmanager.jwt.security.QrJwtProvider;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalQrService {
    private final TicketRepository ticketRepository;
    private final FestivalCuratorRepository festivalCuratorRepository;
    private final QrJwtProvider qrJwtProvider;

    public QrTicketResponse generateFestivalTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Билет не найден"));

        if (!ticket.getUserId().equals(userId)) {
            log.error("Пользователь {} пытался сгенерировать QR для чужого билета {}", userId, ticketId);
            throw new SecurityException("Это не ваш билет");
        }

        Map<String, Object> claims = Map.of(
                "ticket_id", ticket.getId(),
                "fest_loc_id", ticket.getTicketTypeInstance().getFestivalLocation().getId()
        );

        String token = qrJwtProvider.generateQrToken(claims, userId);
        return new QrTicketResponse(token);
    }

    public VerifyQrTicketResponse verifyFestivalTicket(Long festivalLocationId, String token, Long curatorId) {
        if (!festivalCuratorRepository
                .existsByFestivalCuratorId_FestivalLocationIdAndFestivalCuratorId_CuratorId(festivalLocationId, curatorId)) {
            throw new SecurityException("У вас нет прав куратора на этой площадке");
        }

        try {
            Claims claims = qrJwtProvider.extractClaims(token);
            Long tokenTicketId = claims.get("ticket_id", Long.class);
            Long tokenFestLocId = claims.get("fest_loc_id", Long.class);

            Long tokenUserId = null;
            try {
                String subject = claims.getSubject();
                if (subject != null) tokenUserId = Long.parseLong(subject);
            } catch (NumberFormatException ignored) {}

            if (!festivalLocationId.equals(tokenFestLocId)) {
                return VerifyQrTicketResponse.builder()
                        .status(VerifyQrTicketResponse.VerificationStatus.LOCATION_MISMATCH)
                        .targetId(tokenTicketId)
                        .locationId(tokenFestLocId)
                        .userId(tokenUserId)
                        .build();
            }

            return VerifyQrTicketResponse.builder()
                    .status(VerifyQrTicketResponse.VerificationStatus.SUCCESS)
                    .targetId(tokenTicketId)
                    .locationId(tokenFestLocId)
                    .userId(tokenUserId)
                    .build();

        } catch (ExpiredJwtException e) {
            return VerifyQrTicketResponse.builder()
                    .status(VerifyQrTicketResponse.VerificationStatus.EXPIRED)
                    .targetId(e.getClaims().get("ticket_id", Long.class))
                    .build();
        } catch (Exception e) {
            return VerifyQrTicketResponse.builder().status(VerifyQrTicketResponse.VerificationStatus.INVALID).build();
        }
    }
}