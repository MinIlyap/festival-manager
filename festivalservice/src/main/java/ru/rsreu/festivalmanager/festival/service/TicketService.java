package ru.rsreu.festivalmanager.festival.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.festival.client.UserServiceClient;
import ru.rsreu.festivalmanager.festival.dto.*;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;
import ru.rsreu.festivalmanager.festival.entity.ticket.*;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalInstance;
import ru.rsreu.festivalmanager.festival.entity.ticket.Ticket;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketTypeInstance;
import ru.rsreu.festivalmanager.festival.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketTypeInstanceRepository ticketTypeInstanceRepository;
    private final FestivalCompanyRepository festivalCompanyRepository;
    private final FestivalInstanceRepository festivalInstanceRepository;
    private final FestivalLocationRepository festivalLocationRepository;
    private final UserServiceClient userClient;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() { return "Bearer " + systemToken; }

    @Transactional
    public TicketType createTicketType(String name) {
        if (ticketTypeRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Тип билета с таким названием уже существует");
        }

        TicketType ticketType = new TicketType();
        ticketType.setName(name);

        return ticketTypeRepository.save(ticketType);
    }

    @Transactional
    public Long createTicketTypeInstance(TicketTypeInstanceCreateRequest request, Long companyId) {
        // 1. Проверяем, есть ли права у компании на этот фестиваль
        if (!festivalCompanyRepository.existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(
                request.getFestivalLocationId(), companyId)) {
            throw new SecurityException("У вашей компании нет прав на управление билетами этого фестиваля");
        }

        // 2. Достаем глобальный тип билета
        TicketType ticketType = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Глобальный тип билета не найден"));

        // 3. Достаем локацию фестиваля
        FestivalLocation location = festivalLocationRepository.findById(request.getFestivalLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        // 4. Защита от дублей: проверяем, не добавлен ли уже этот тип билета на этот фестиваль
        boolean alreadyExists = ticketTypeInstanceRepository.existsByFestivalLocationAndTicketType(location, ticketType);
        if (alreadyExists) {
            throw new IllegalArgumentException("Этот тип билета уже добавлен на данный фестиваль");
        }

        // 5. Создаем инстанс
        TicketTypeInstance instance = new TicketTypeInstance();
        instance.setTicketType(ticketType);
        instance.setFestivalLocation(location);
        instance.setDescription(request.getDescription());
        instance.setPrice(request.getPrice());

        instance = ticketTypeInstanceRepository.save(instance);
        return instance.getId();
    }

    @Transactional
    public void updateTicketTypeInstance(Long instanceId, String description, BigDecimal price, Long companyId) {
        TicketTypeInstance instance = ticketTypeInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Экземпляр билета не найден"));

        // Проверка прав компании на этот фестиваль
        Long festivalLocationId = instance.getFestivalLocation().getId();
        if (!festivalCompanyRepository.existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, companyId)) {
            throw new SecurityException("У вашей компании нет прав на редактирование билетов этого фестиваля");
        }

        instance.setDescription(description);
        instance.setPrice(price);
        ticketTypeInstanceRepository.save(instance);
    }

    @Transactional
    public void deleteTicketTypeInstance(Long instanceId, Long companyId) {
        TicketTypeInstance instance = ticketTypeInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Экземпляр билета не найден"));

        Long festivalLocationId = instance.getFestivalLocation().getId();
        if (!festivalCompanyRepository.existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, companyId)) {
            throw new SecurityException("У вашей компании нет прав на удаление билетов этого фестиваля");
        }

        // КРИТИЧЕСКАЯ ПРОВЕРКА: Если билеты уже куплены, удалять нельзя
        if (ticketRepository.existsByTicketTypeInstanceId(instanceId)) {
            throw new IllegalArgumentException("Невозможно удалить тип билета: по нему уже есть проданные билеты");
        }

        ticketTypeInstanceRepository.delete(instance);
    }

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

        int userAge = Period.between(userProfile.getDateOfBirth(), LocalDate.now()).getYears();
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
    public Boolean hasValidTicket(Long festivalLocationId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return ticketRepository.existsByUserIdAndTicketTypeInstance_FestivalLocation_IdAndExpiryDateAfter(
                userId, festivalLocationId, now
        );
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