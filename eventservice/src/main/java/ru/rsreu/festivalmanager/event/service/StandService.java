package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.rsreu.festivalmanager.event.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.event.dto.FestivalStandCreateRequest;
import ru.rsreu.festivalmanager.event.dto.FestivalStandResponse;
import ru.rsreu.festivalmanager.event.dto.ProductAddRequest;
import ru.rsreu.festivalmanager.event.entity.stand.*;
import ru.rsreu.festivalmanager.event.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandService {

    private final FestivalStandRepository festivalStandRepository;
    private final StandProductRepository standProductRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final FestivalServiceClient festivalServiceClient;
    private final ProductRepository productRepository;
    private final StandRepository standRepository;
    private final StandInstanceRepository standInstanceRepository;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }


    @Transactional
    public void detachStandFromFestival(Long festivalStandId, Long companyId) {
        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));

        // Защита: только владелец стенда может его открепить
        if (!festivalStand.getStandInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("У вас нет прав на редактирование этого стенда");
        }

        // Защита: проверяем статус фестиваля через Feign Client
        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), festivalStand.getFestivalLocationId()
        );

        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException("Нельзя открепить стенд: фестиваль уже опубликован!");
        }

        // Если все ок - удаляем. CascadeType.ALL, если бы он был настроен, удалил бы и StandInstance,
        // но здесь мы просто разрываем связь (или удаляем сам фестивальный стенд)
        festivalStandRepository.delete(festivalStand);
    }

    @Transactional
    public void addProductToStand(Long festivalStandId, Long companyId, ProductAddRequest request) {

        // 1. Проверяем права на стенд
        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));

        if (!festivalStand.getStandInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("Это не ваш стенд");
        }

        // 2. Ищем базовый продукт в словаре
        Product baseProduct = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Базовый продукт не найден в словаре"));

        // 3. Ищем точную копию инстанса продукта ИЛИ создаем новую
        ProductInstance productInstance = productInstanceRepository.findIdenticalInstance(
                request.getProductId(),
                request.getQuantity(),
                request.getPrice(),
                request.getDescription()
        ).orElseGet(() -> {
            ProductInstance newInstance = ProductInstance.builder()
                    .product(baseProduct)
                    .description(request.getDescription())
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .build();
            return productInstanceRepository.save(newInstance);
        });

        // 4. Связываем продукт со стендом через составной ключ
        StandProduct standProduct = new StandProduct(
                new StandProductId(festivalStand, productInstance)
        );
        standProductRepository.save(standProduct);
    }

    @Transactional(readOnly = true)
    public Page<FestivalStandResponse> searchStands(String name, Long cityId, LocalDate dateStart, StandType type, Pageable pageable) {
        List<Long> locationIds = null;

        if (cityId != null) {
            locationIds = festivalServiceClient.getFestivalLocationIdsByCity(getSystemToken(), cityId);
            if (locationIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        return festivalStandRepository.searchStands(name, locationIds, type, dateStart, pageable)
                .map(fs -> FestivalStandResponse.builder()
                        .id(fs.getId())
                        .festivalLocationId(fs.getFestivalLocationId())
                        .name(fs.getStandInstance().getStand().getName())
                        .type(fs.getStandInstance().getStandType())
                        .description(fs.getStandInstance().getDescription())
                        .companyId(fs.getStandInstance().getCompanyId())
                        .dateStart(fs.getStandInstance().getDateStart())
                        .build());
    }

    @Transactional(readOnly = true)
    public List<FestivalStandResponse> getStandsByFestival(Long festivalLocationId) {
        return festivalStandRepository.findAllByFestivalLocationId(festivalLocationId)
                .stream()
                .map(fs -> FestivalStandResponse.builder()
                        .id(fs.getId())
                        .festivalLocationId(fs.getFestivalLocationId())
                        .name(fs.getStandInstance().getStand().getName())
                        .type(fs.getStandInstance().getStandType())
                        .description(fs.getStandInstance().getDescription())
                        .companyId(fs.getStandInstance().getCompanyId())
                        .dateStart(fs.getStandInstance().getDateStart())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createFestivalStand(Long companyId, FestivalStandCreateRequest request) {
        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), request.getFestivalLocationId()
        );
        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException("Нельзя добавить стенд: фестиваль уже опубликован");
        }

        Stand stand = standRepository.findById(request.getStandId())
                .orElseThrow(() -> new IllegalArgumentException("Базовый стенд не найден"));

        // 2. Ищем точную копию инстанса ИЛИ создаем новую
        StandInstance instance = standInstanceRepository.findIdenticalInstance(
                request.getStandId(),
                companyId,
                request.getType(),
                request.getDateStart(),
                request.getDescription()
        ).orElseGet(() -> {
            // Если не нашли - собираем новый
            StandInstance newInstance = StandInstance.builder()
                    .stand(stand)
                    .companyId(companyId)
                    .standType(request.getType())
                    .description(request.getDescription())
                    .dateStart(request.getDateStart())
                    .build();
            return standInstanceRepository.save(newInstance);
        });

        // 3. Привязываем инстанс к локации фестиваля
        FestivalStand festivalStand = FestivalStand.builder()
                .festivalLocationId(request.getFestivalLocationId())
                .standInstance(instance)
                .build();

        return festivalStandRepository.save(festivalStand).getId();
    }
}
