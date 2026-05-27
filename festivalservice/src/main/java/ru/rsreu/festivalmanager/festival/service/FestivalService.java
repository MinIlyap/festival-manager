package ru.rsreu.festivalmanager.festival.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.festival.client.AuthServiceClient;
import ru.rsreu.festivalmanager.festival.client.CompanyServiceClient;
import ru.rsreu.festivalmanager.festival.client.LocationServiceClient;
import ru.rsreu.festivalmanager.festival.client.UserServiceClient;
import ru.rsreu.festivalmanager.festival.dto.*;
import ru.rsreu.festivalmanager.festival.entity.festival.*;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketType;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketTypeInstance;
import ru.rsreu.festivalmanager.festival.event.FestivalEvents;
import ru.rsreu.festivalmanager.festival.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalInstanceRepository instanceRepository;
    private final FestivalLocationRepository locationRepository;
    private final TicketTypeInstanceRepository ticketTypeInstanceRepository;
    private final AgeLimitRepository ageLimitRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final FestivalCompanyRepository festivalCompanyRepository;
    private final FestivalCuratorRepository festivalCuratorRepository;
    private final CompanyServiceClient companyClient;
    private final AuthServiceClient authClient;
    private final ApplicationEventPublisher eventPublisher;
    private final LocationServiceClient locationClient;
    private final UserServiceClient userServiceClient;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }

    @Transactional
    public Long applyForFestival(FestivalCreateRequest request, Long senderCompanyId) {
        boolean platformExists = locationClient.checkPlatformExists(getSystemToken(), request.getPlatformInstanceId());

        if (!platformExists) {
            throw new IllegalArgumentException("Указанная площадка не существует в Location Service");
        }

        AgeLimit ageLimit = ageLimitRepository.findById(request.getAgeLimitId())
                .orElseThrow(() -> new IllegalArgumentException("Возрастное ограничение не найдено"));

        // 1. Ищем или создаем сам фестиваль (название)
        Festival festival = festivalRepository.findByNameIgnoreCase(request.getFestivalName())
                .orElseGet(() -> {
                    Festival f = new Festival();
                    f.setName(request.getFestivalName());
                    return festivalRepository.save(f);
                });

        // 2. ИСправлено: Ищем существующий инстанс (проведение) по датам, иначе создаем новый
        FestivalInstance instance = instanceRepository
                .findByFestivalIdAndDateStartAndDateEnd(festival.getId(), request.getDateStart(), request.getDateEnd())
                .orElseGet(() -> getFestivalInstance(request, festival, ageLimit));

        instance = instanceRepository.save(instance);

        FestivalLocation location = new FestivalLocation();
        location.setFestivalInstance(instance);
        location.setPlatformInstanceId(request.getPlatformInstanceId());
        location.setCreatedAt(OffsetDateTime.now());
        location = locationRepository.save(location);

        List<Long> companyIds = new ArrayList<>();
        companyIds.add(senderCompanyId);

        if (request.getOrganizerTins() != null) {
            for (String tin : request.getOrganizerTins()) {
                try {
                    CompanyDto company = companyClient.getCompanyByTin(getSystemToken(), tin);
                    if (!companyIds.contains(company.getId())) companyIds.add(company.getId());
                } catch (Exception e) {
                    CompanyCreateDto newCompany = new CompanyCreateDto(tin, "Неизвестная", "Неизвестный", "temp_" + tin + "@mail.com");
                    Long newCompanyId = companyClient.createInactiveCompany(getSystemToken(), newCompany);
                    companyIds.add(newCompanyId);
                }
            }
        }

        for (Long companyId : companyIds) {
            FestivalCompanyId fcId = new FestivalCompanyId(location, companyId);
            festivalCompanyRepository.save(new FestivalCompany(fcId));
        }

        List<Long> curatorIds = new ArrayList<>();
        if (request.getCuratorEmails() != null) {
            for (String email : request.getCuratorEmails()) {
                try {
                    AuthCredentialDto credential = authClient.getUserByEmail(getSystemToken(), email);
                    if ("COMPANY".equals(credential.getAccessType())) {
                        throw new IllegalArgumentException("Email " + email + " принадлежит компании, а не пользователю");
                    }
                    if (!curatorIds.contains(credential.getUserId())) curatorIds.add(credential.getUserId());
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Пользователь с email " + email + " не найден в системе.");
                }
            }
        }

        for (Long curatorId : curatorIds) {
            FestivalCuratorId curId = new FestivalCuratorId(location, curatorId);
            festivalCuratorRepository.save(new FestivalCurator(curId));
        }

        if (request.getTicketTypes() != null) {
            for (TicketTypeRequest ttReq : request.getTicketTypes()) {
                TicketType ticketType = ticketTypeRepository.findById(ttReq.getTicketTypeId())
                        .orElseThrow(() -> new IllegalArgumentException("Тип билета не найден: " + ttReq.getTicketTypeId()));

                TicketTypeInstance tti = new TicketTypeInstance();
                tti.setFestivalLocation(location);
                tti.setTicketType(ticketType);
                tti.setDescription(ttReq.getDescription());
                tti.setPrice(ttReq.getPrice());
                ticketTypeInstanceRepository.save(tti);
            }
        }

        return location.getId();
    }

    private static @NonNull FestivalInstance getFestivalInstance(FestivalCreateRequest request, Festival festival, AgeLimit ageLimit) {
        FestivalInstance instance = new FestivalInstance();
        instance.setFestival(festival);
        instance.setAgeLimit(ageLimit);
        instance.setDescription(request.getDescription());
        instance.setDateStart(request.getDateStart());
        instance.setTimeStart(request.getTimeStart());
        instance.setDateEnd(request.getDateEnd());
        instance.setTimeEnd(request.getTimeEnd());
        instance.setMaxPeopleNumber(request.getMaxPeopleNumber());
        instance.setVisible(false);
        return instance;
    }
    @Transactional
    public void submitForModeration(Long festivalLocationId, Long requestingCompanyId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (location.getFestivalInstance().isVisible()) {
            throw new IllegalStateException("Фестиваль уже опубликован");
        }

        boolean isOrganizer = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, requestingCompanyId);
        if (!isOrganizer) {
            throw new IllegalStateException("Только организатор может отправить заявку на модерацию");
        }

        eventPublisher.publishEvent(new FestivalEvents.FestivalAppRequestEvent(
                festivalLocationId,
                location.getFestivalInstance().getFestival().getName(),
                requestingCompanyId,
                OffsetDateTime.now()
        ));
    }

    @Transactional
    public void processApplicationResponse(Long festivalLocationId, String status) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if ("APPROVED".equalsIgnoreCase(status)) {
            location.getFestivalInstance().setVisible(true);
            locationRepository.save(location);

            List<Long> curatorIds = festivalCuratorRepository.findAllByFestivalCuratorId_FestivalLocation(location)
                    .stream()
                    .map(fc -> fc.getFestivalCuratorId().getCuratorId())
                    .collect(Collectors.toList());

            if (!curatorIds.isEmpty()) {
                try {
                    authClient.assignCuratorRoles(getSystemToken(), curatorIds);
                } catch (Exception e) {
                    log.error("Не удалось назначить роли кураторов для фестиваля {}: {}", festivalLocationId, e.getMessage());
                    // Одобрение фестиваля не откатываем — роли куратора можно назначить повторно
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FestivalResponse> searchFestivals(String name, Long cityId, LocalDate startDate, LocalDate endDate) {
        List<FestivalLocation> locations = locationRepository.searchActiveFestivals(name, startDate, endDate);

        if (cityId != null) {
            List<Long> platformIdsInCity = locationClient.getPlatformIdsByCity(getSystemToken(), cityId);
            locations = locations.stream()
                    .filter(loc -> platformIdsInCity.contains(loc.getPlatformInstanceId()))
                    .collect(Collectors.toList());
        }

        return locations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FestivalResponse> getFestivalsByCompany(Long companyId) {
        return festivalCompanyRepository.findAllByFestivalCompanyId_CompanyId(companyId)
                .stream()
                .map(fc -> mapToResponse(fc.getFestivalCompanyId().getFestivalLocation()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FestivalResponse> getFestivalsByCurator(Long curatorId) {
        return festivalCuratorRepository.findAllByFestivalCuratorId_CuratorId(curatorId)
                .stream()
                .map(fc -> mapToResponse(fc.getFestivalCuratorId().getFestivalLocation()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FestivalCreateRequest getFestivalTemplate(String festivalName) {
        Festival festival = festivalRepository.findByNameIgnoreCase(festivalName)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        FestivalInstance latestInstance = instanceRepository.findByFestivalIdOrderByDateStartDesc(festival.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет предыдущих проведений"));

        FestivalLocation location = locationRepository.findByFestivalInstanceId(latestInstance.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Локация не найдена"));

        FestivalCreateRequest template = new FestivalCreateRequest();
        template.setFestivalName(festival.getName());
        template.setAgeLimitId(latestInstance.getAgeLimit().getId());
        template.setDescription(latestInstance.getDescription());
        template.setMaxPeopleNumber(latestInstance.getMaxPeopleNumber());

        template.setPlatformInstanceId(location.getPlatformInstanceId());

        template.setDateStart(null);
        template.setDateEnd(null);
        template.setTimeStart(latestInstance.getTimeStart());
        template.setTimeEnd(latestInstance.getTimeEnd());

        List<String> oldTins = festivalCompanyRepository.findAllByFestivalCompanyId_FestivalLocation(location)
                .stream()
                .map(fc -> {
                    try {
                        return companyClient.getCompanyById(getSystemToken(), fc.getFestivalCompanyId().getCompanyId()).getTin();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(tin -> tin != null)
                .collect(Collectors.toList());
        template.setOrganizerTins(oldTins);

        template.setCuratorEmails(new ArrayList<>());

        List<TicketTypeRequest> oldTickets = ticketTypeInstanceRepository.findByFestivalLocation(location)
                .stream()
                .map(tti -> {
                    TicketTypeRequest tr = new TicketTypeRequest();
                    tr.setTicketTypeId(tti.getTicketType().getId());
                    tr.setDescription(tti.getDescription());
                    tr.setPrice(tti.getPrice());
                    return tr;
                })
                .collect(Collectors.toList());
        template.setTicketTypes(oldTickets);

        return template;
    }

    @Transactional(readOnly = true)
    public List<Long> getFestivalLocationIdsByCity(Long cityId) {
        List<Long> platformIdsInCity = locationClient.getPlatformIdsByCity(getSystemToken(), cityId);

        if (platformIdsInCity.isEmpty()) {
            return Collections.emptyList();
        }

        return locationRepository.findAllVisibleByPlatformInstanceIdIn(platformIdsInCity)
                .stream()
                .map(FestivalLocation::getId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getAllVisibleFestivalLocationIds() {
        return locationRepository.findAllVisibleIds();
    }

    @Transactional(readOnly = true)
    public String getFestivalName(Long festivalLocationId) {
        return locationRepository.findById(festivalLocationId)
                .map(loc -> loc.getFestivalInstance().getFestival().getName())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Boolean isFestivalVisible(Long festivalLocationId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        return location.getFestivalInstance().isVisible();
    }

    @Transactional(readOnly = true)
    public FestivalResponse getFestivalById(Long festivalLocationId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));
        return mapToResponse(location);
    }

    @Transactional(readOnly = true)
    public List<AgeLimit> getAgeLimits() {
        return ageLimitRepository.findAll();
    }

    public CompanyDto searchCompanyByTin(String tin) {
        return companyClient.getCompanyByTin(getSystemToken(), tin);
    }

    public CompanyDto getCompanyById(Long id) {
        return companyClient.getCompanyById(getSystemToken(), id);
    }

    @Transactional
    public CompanyDto createCompany(CompanyCreateDto dto) {
        Long companyId = companyClient.createInactiveCompany(getSystemToken(), dto);
        return companyClient.getCompanyById(getSystemToken(), companyId);
    }

    @Transactional(readOnly = true)
    public Boolean isCompanyFestivalOrganizer(Long festivalLocationId, Long companyId) {
        return festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, companyId);
    }

    @Transactional(readOnly = true)
    public Boolean hasAccessForFestival(String entityType, Long entityId, Long companyId) {
        if ("FESTIVAL_LOCATION".equals(entityType)) {
            return festivalCompanyRepository
                    .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(entityId, companyId);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<FestivalOrganizerDto> getOrganizers(Long festivalLocationId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        return festivalCompanyRepository.findAllByFestivalCompanyId_FestivalLocation(location)
                .stream()
                .map(fc -> {
                    Long companyId = fc.getFestivalCompanyId().getCompanyId();
                    try {
                        CompanyDto company = companyClient.getCompanyById(getSystemToken(), companyId);
                        return FestivalOrganizerDto.builder()
                                .companyId(companyId)
                                .tin(company.getTin())
                                .brandName(company.getBrandName())
                                .fullName(company.getFullName())
                                .email(company.getEmail())
                                .build();
                    } catch (Exception e) {
                        return FestivalOrganizerDto.builder().companyId(companyId).build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FestivalOrganizerDto addOrganizer(Long festivalLocationId, String tin, Long requestingCompanyId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (location.getFestivalInstance().isVisible()) {
            throw new IllegalStateException("Нельзя редактировать опубликованный фестиваль");
        }

        boolean isOrganizer = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, requestingCompanyId);
        if (!isOrganizer) {
            throw new IllegalStateException("Только организатор фестиваля может добавлять компании");
        }

        CompanyDto company = companyClient.getCompanyByTin(getSystemToken(), tin);

        boolean alreadyAdded = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, company.getId());
        if (alreadyAdded) {
            throw new IllegalArgumentException("Компания уже является организатором этого фестиваля");
        }

        FestivalCompanyId fcId = new FestivalCompanyId(location, company.getId());
        festivalCompanyRepository.save(new FestivalCompany(fcId));

        return FestivalOrganizerDto.builder()
                .companyId(company.getId())
                .tin(company.getTin())
                .brandName(company.getBrandName())
                .fullName(company.getFullName())
                .email(company.getEmail())
                .build();
    }

    @Transactional
    public void removeOrganizer(Long festivalLocationId, Long companyId, Long requestingCompanyId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (location.getFestivalInstance().isVisible()) {
            throw new IllegalStateException("Нельзя редактировать опубликованный фестиваль");
        }

        boolean isOrganizer = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, requestingCompanyId);
        if (!isOrganizer) {
            throw new IllegalStateException("Только организатор фестиваля может удалять компании");
        }

        FestivalCompanyId fcId = new FestivalCompanyId(location, companyId);
        if (!festivalCompanyRepository.existsById(fcId)) {
            throw new IllegalArgumentException("Компания не является организатором этого фестиваля");
        }

        festivalCompanyRepository.deleteById(fcId);
    }

    @Transactional(readOnly = true)
    public List<FestivalCuratorDto> getCurators(Long festivalLocationId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        return festivalCuratorRepository.findAllByFestivalCuratorId_FestivalLocation(location)
                .stream()
                .map(fc -> {
                    Long curatorId = fc.getFestivalCuratorId().getCuratorId();
                    String email = null;
                    String nickname = null;
                    try {
                        AuthCredentialDto credential = authClient.getCredentialByUserId(getSystemToken(), curatorId);
                        email = credential.getEmail();
                    } catch (Exception ignored) {}
                    try {
                        UserProfileDto profile = userServiceClient.getUserProfile(getSystemToken(), curatorId);
                        nickname = profile.getNickname();
                    } catch (Exception ignored) {}
                    return FestivalCuratorDto.builder()
                            .userId(curatorId)
                            .email(email)
                            .nickname(nickname)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FestivalCuratorDto addCurator(Long festivalLocationId, String email, Long requestingCompanyId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (location.getFestivalInstance().isVisible()) {
            throw new IllegalStateException("Нельзя редактировать опубликованный фестиваль");
        }

        boolean isOrganizer = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, requestingCompanyId);
        if (!isOrganizer) {
            throw new IllegalStateException("Только организатор фестиваля может добавлять кураторов");
        }

        AuthCredentialDto credential = authClient.getUserByEmail(getSystemToken(), email);
        if ("COMPANY".equals(credential.getAccessType())) {
            throw new IllegalArgumentException("Email " + email + " принадлежит компании, а не пользователю");
        }

        boolean alreadyAdded = festivalCuratorRepository
                .existsByFestivalCuratorId_FestivalLocationIdAndFestivalCuratorId_CuratorId(festivalLocationId, credential.getUserId());
        if (alreadyAdded) {
            throw new IllegalArgumentException("Пользователь уже является куратором этого фестиваля");
        }

        FestivalCuratorId curId = new FestivalCuratorId(location, credential.getUserId());
        festivalCuratorRepository.save(new FestivalCurator(curId));

        return FestivalCuratorDto.builder()
                .userId(credential.getUserId())
                .email(credential.getEmail())
                .build();
    }

    @Transactional
    public void removeCurator(Long festivalLocationId, Long curatorId, Long requestingCompanyId) {
        FestivalLocation location = locationRepository.findById(festivalLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (location.getFestivalInstance().isVisible()) {
            throw new IllegalStateException("Нельзя редактировать опубликованный фестиваль");
        }

        boolean isOrganizer = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(festivalLocationId, requestingCompanyId);
        if (!isOrganizer) {
            throw new IllegalStateException("Только организатор фестиваля может удалять кураторов");
        }

        FestivalCuratorId curId = new FestivalCuratorId(location, curatorId);
        if (!festivalCuratorRepository.existsById(curId)) {
            throw new IllegalArgumentException("Пользователь не является куратором этого фестиваля");
        }

        festivalCuratorRepository.deleteById(curId);
    }

    @Transactional
    public void updateFestival(Long locationId, FestivalUpdateRequest request, Long companyId) {
        FestivalLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Фестиваль не найден"));

        if (location.getFestivalInstance().isVisible()) {
            throw new IllegalStateException("Нельзя редактировать опубликованный фестиваль");
        }

        boolean isOrganizer = festivalCompanyRepository
                .existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(locationId, companyId);
        if (!isOrganizer) {
            throw new IllegalStateException("Только организатор может редактировать фестиваль");
        }

        AgeLimit ageLimit = ageLimitRepository.findById(request.getAgeLimitId())
                .orElseThrow(() -> new IllegalArgumentException("Возрастное ограничение не найдено"));

        Festival festival = location.getFestivalInstance().getFestival();
        festival.setName(request.getFestivalName());
        festivalRepository.save(festival);

        FestivalInstance instance = location.getFestivalInstance();
        instance.setDescription(request.getDescription() != null ? request.getDescription() : "");
        instance.setDateStart(request.getDateStart());
        instance.setTimeStart(request.getTimeStart());
        instance.setDateEnd(request.getDateEnd());
        instance.setTimeEnd(request.getTimeEnd());
        instance.setMaxPeopleNumber(request.getMaxPeopleNumber());
        instance.setAgeLimit(ageLimit);
        instanceRepository.save(instance);
    }

    private FestivalResponse mapToResponse(FestivalLocation location) {
        FestivalInstance instance = location.getFestivalInstance();
        int ageRating = 0;
        if (instance.getAgeLimit() != null && instance.getAgeLimit().getAgeLimitValue() != null) {
            try {
                ageRating = Integer.parseInt(instance.getAgeLimit().getAgeLimitValue().replace("+", "").trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String cityName = null;
        String platformName = null;
        String address = null;
        if (location.getPlatformInstanceId() != null) {
            try {
                ru.rsreu.festivalmanager.festival.dto.PlatformInfoDto info =
                        locationClient.getPlatformInstanceById(getSystemToken(), location.getPlatformInstanceId());
                if (info != null) {
                    platformName = info.getPlatformName();
                    if (info.getCity() != null) cityName = info.getCity().getName();
                    if (info.getStreet() != null) {
                        address = info.getStreet() + (info.getHouse() != null ? ", " + info.getHouse() : "");
                    }
                }
            } catch (Exception ignored) {}
        }

        return FestivalResponse.builder()
                .festivalLocationId(location.getId())
                .name(instance.getFestival().getName())
                .description(instance.getDescription())
                .dateStart(instance.getDateStart())
                .timeStart(instance.getTimeStart())
                .dateEnd(instance.getDateEnd())
                .timeEnd(instance.getTimeEnd())
                .platformInstanceId(location.getPlatformInstanceId())
                .isVisible(instance.isVisible())
                .ageRating(ageRating)
                .ageLimitId(instance.getAgeLimit() != null ? instance.getAgeLimit().getId() : null)
                .maxPeopleNumber(instance.getMaxPeopleNumber())
                .cityName(cityName)
                .platformName(platformName)
                .address(address)
                .build();
    }
}
