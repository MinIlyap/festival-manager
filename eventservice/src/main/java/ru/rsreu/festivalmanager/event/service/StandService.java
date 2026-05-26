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
import ru.rsreu.festivalmanager.event.dto.FestivalStandUpdateRequest;
import ru.rsreu.festivalmanager.event.dto.ProductAddRequest;
import ru.rsreu.festivalmanager.event.dto.StandProductResponse;
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
    public void updateFestivalStand(Long festivalStandId, Long companyId, FestivalStandUpdateRequest request) {
        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));

        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), festivalStand.getFestivalLocationId()
        );
        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException("Нельзя редактировать стенд: фестиваль уже опубликован");
        }

        StandInstance instance = festivalStand.getStandInstance();
        instance.setStandType(request.getType());
        instance.setDescription(request.getDescription() != null ? request.getDescription() : "");
        instance.setDateStart(request.getDateStart());
        if (request.getOrganizerCompanyId() != null) {
            instance.setCompanyId(request.getOrganizerCompanyId());
        }
        standInstanceRepository.save(instance);
    }

    @Transactional
    public void detachStandFromFestival(Long festivalStandId, Long companyId) {
        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));

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
    public List<StandProductResponse> getProductsByStand(Long festivalStandId) {
        return standProductRepository.findAllByStandProductId_FestivalStand_Id(festivalStandId)
                .stream()
                .map(sp -> {
                    ProductInstance pi = sp.getStandProductId().getProductInstance();
                    return StandProductResponse.builder()
                            .productInstanceId(pi.getId())
                            .productId(pi.getProduct().getId())
                            .productName(pi.getProduct().getName())
                            .description(pi.getDescription())
                            .quantity(pi.getQuantity())
                            .price(pi.getPrice())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeProductFromStand(Long festivalStandId, Long productInstanceId, Long companyId) {
        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));
        if (!festivalStand.getStandInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("Это не ваш стенд");
        }
        StandProduct sp = standProductRepository
                .findByStandProductId_FestivalStand_IdAndStandProductId_ProductInstance_Id(festivalStandId, productInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден на стенде"));
        standProductRepository.delete(sp);
    }

    @Transactional
    public void updateProductOnStand(Long festivalStandId, Long oldProductInstanceId, Long companyId, ProductAddRequest request) {
        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));
        if (!festivalStand.getStandInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("Это не ваш стенд");
        }
        StandProduct oldSp = standProductRepository
                .findByStandProductId_FestivalStand_IdAndStandProductId_ProductInstance_Id(festivalStandId, oldProductInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден на стенде"));
        standProductRepository.delete(oldSp);

        Product baseProduct = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Базовый продукт не найден в словаре"));

        ProductInstance productInstance = productInstanceRepository.findIdenticalInstance(
                request.getProductId(), request.getQuantity(), request.getPrice(), request.getDescription()
        ).orElseGet(() -> productInstanceRepository.save(ProductInstance.builder()
                .product(baseProduct)
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build()));

        standProductRepository.save(new StandProduct(new StandProductId(festivalStand, productInstance)));
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
                        .organizerCompanyId(fs.getStandInstance().getCompanyId())
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
                        .organizerCompanyId(fs.getStandInstance().getCompanyId())
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

        // companyId хранит компанию-организатора; если не задана в запросе — создающая компания
        Long organizerCompanyId = request.getOrganizerCompanyId() != null ? request.getOrganizerCompanyId() : companyId;

        // 2. Ищем точную копию инстанса ИЛИ создаем новую
        StandInstance instance = standInstanceRepository.findIdenticalInstance(
                request.getStandId(),
                organizerCompanyId,
                request.getType(),
                request.getDateStart(),
                request.getDescription()
        ).orElseGet(() -> {
            // Если не нашли - собираем новый
            StandInstance newInstance = StandInstance.builder()
                    .stand(stand)
                    .companyId(organizerCompanyId)
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
