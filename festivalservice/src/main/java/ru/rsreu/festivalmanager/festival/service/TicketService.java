package ru.rsreu.festivalmanager.festival.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.festival.client.UserServiceClient;
import ru.rsreu.festivalmanager.festival.dto.*;
import ru.rsreu.festivalmanager.festival.entity.*;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalInstance;
import ru.rsreu.festivalmanager.festival.entity.ticket.Ticket;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketTypeInstance;
import ru.rsreu.festivalmanager.festival.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTypeInstanceRepository ticketTypeInstanceRepository;
    private final FestivalInstanceRepository festivalInstanceRepository;
    private final UserServiceClient userClient;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() { return "Bearer " + systemToken; }

    @Transactional
    public Long purchaseTicket(Long userId, TicketPurchaseRequest request) {
        boolean isBlacklisted = userClient.checkBlackListStatus(getSystemToken(), userId);
        if (isBlacklisted) throw new IllegalArgumentException("Вы находитесь в черном списке.");

        TicketTypeInstance ticketTypeInstance = ticketTypeInstanceRepository.findById(request.getTicketTypeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("Тип билета не найден"));

        FestivalInstance festivalInstance = ticketTypeInstance.getFestivalLocation().getFestivalInstance();
        String ageLimitStr = festivalInstance.getAgeLimit().getAgeLimitValue();

        UserProfileDto userProfile = userClient.getUserProfile(getSystemToken(), userId);

        if (userProfile.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Для покупки билета необходимо указать дату рождения в профиле");
        }

        int userAge = java.time.Period.between(userProfile.getDateOfBirth(), java.time.LocalDate.now()).getYears();
        int requiredAge = 0;

        try {
            requiredAge = Integer.parseInt(ageLimitStr.replace("+", ""));
        } catch (NumberFormatException e) {
            log.warn("Нестандартный формат возрастного ограничения: {}", ageLimitStr);
        }

        if (userAge < requiredAge) {
            throw new IllegalArgumentException(
                    String.format("Ваш возраст (%d) не соответствует возрастному ограничению фестиваля (%s)", userAge, ageLimitStr)
            );
        }

        if (userProfile.getPhone() == null || userProfile.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Для покупки билета необходимо указать номер телефона в профиле");
        }

        if (requiredAge >= 16) {
            if (userProfile.getPassportData() == null || userProfile.getPassportData().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Для покупки билета на фестиваль категории " + ageLimitStr + " необходимо заполнить паспортные данные в профиле"
                );
            }
        }

        Long instanceId = festivalInstance.getId();
        FestivalInstance lockedInstance = festivalInstanceRepository.findByIdWithPessimisticLock(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (!lockedInstance.isVisible()) {
            throw new IllegalArgumentException("Продажа билетов на этот фестиваль закрыта");
        }

        Integer availableTickets = lockedInstance.getMaxPeopleNumber() - ticketRepository.sumSoldTicketsByFestivalInstanceId(instanceId);
        if (request.getQuantity() > availableTickets) {
            throw new IllegalArgumentException("Недостаточно билетов. Осталось: " + availableTickets);
        }

        BigDecimal finalPrice = ticketTypeInstance.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Ticket ticket = Ticket.builder()
                .userId(userId)
                .ticketTypeInstance(ticketTypeInstance)
                .quantity(request.getQuantity())
                .finalPrice(finalPrice)
                .expiryDate(java.time.LocalDateTime.of(lockedInstance.getDateEnd(), lockedInstance.getTimeEnd()))
                .createdAt(OffsetDateTime.now())
                .build();

        ticketRepository.save(ticket);

        log.info("Пользователь {} (возраст {}) успешно купил {} билетов на фестиваль ID {}",
                userId, userAge, request.getQuantity(), instanceId);

        return ticket.getId();
    }

    @Transactional(readOnly = true)
    public UserTicketsResponse getUserTickets(Long userId) {
        List<Ticket> allTickets = ticketRepository.findAllByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        List<TicketResponse> validTickets = new ArrayList<>();
        List<TicketResponse> expiredTickets = new ArrayList<>();

        for (Ticket t : allTickets) {
            TicketResponse tr = TicketResponse.builder()
                    .ticketId(t.getId())
                    .festivalName(t.getTicketTypeInstance().getFestivalLocation().getFestivalInstance().getFestival().getName())
                    .ticketType(t.getTicketTypeInstance().getTicketType().getName())
                    .quantity(t.getQuantity())
                    .finalPrice(t.getFinalPrice())
                    .expiryDate(t.getExpiryDate())
                    .build();

            if (t.getExpiryDate().isBefore(now)) {
                expiredTickets.add(tr);
            } else {
                validTickets.add(tr);
            }
        }

        return UserTicketsResponse.builder()
                .validTickets(validTickets)
                .expiredTickets(expiredTickets)
                .build();
    }
}