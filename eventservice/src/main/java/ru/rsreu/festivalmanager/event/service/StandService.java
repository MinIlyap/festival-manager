package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.rsreu.festivalmanager.event.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandCreateRequest;
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandResponse;
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandUpdateRequest;
import ru.rsreu.festivalmanager.event.dto.stand.ProductAddRequest;
import ru.rsreu.festivalmanager.event.dto.stand.StandProductResponse;
import ru.rsreu.festivalmanager.event.entity.stand.*;
import ru.rsreu.festivalmanager.event.repository.*;
import ru.rsreu.festivalmanager.event.mapper.StandMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandService {
    private final FestivalStandRepository festivalStandRepository;
    private final StandProductRepository standProductRepository;
    private final StandMapper standMapper;
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

        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), festivalStand.getFestivalLocationId()
        );

        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException("Нельзя открепить стенд: фестиваль уже опубликован!");
        }

        festivalStandRepository.delete(festivalStand);
    }

    @Transactional
    public void addProductToStand(Long festivalStandId, Long companyId, ProductAddRequest request) {

        FestivalStand festivalStand = festivalStandRepository.findById(festivalStandId)
                .orElseThrow(() -> new IllegalArgumentException("Стенд не найден"));

        if (!festivalStand.getStandInstance().getCompanyId().equals(companyId)) {
            throw new IllegalStateException("Это не ваш стенд");
        }

        Product baseProduct = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Базовый продукт не найден в словаре"));

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

        StandProduct standProduct = new StandProduct(
                new StandProductId(festivalStand, productInstance)
        );
        standProductRepository.save(standProduct);
    }

    @Transactional(readOnly = true)
    public List<StandProductResponse> getProductsByStand(Long festivalStandId) {
        return standProductRepository.findAllByStandProductId_FestivalStand_Id(festivalStandId)
                .stream()
                .map(standMapper::toStandProductResponse)
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
        List<Long> locationIds;

        if (cityId != null) {
            locationIds = festivalServiceClient.getFestivalLocationIdsByCity(getSystemToken(), cityId);
        } else {
            locationIds = festivalServiceClient.getAllVisibleFestivalLocationIds(getSystemToken());
        }

        if (locationIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return festivalStandRepository.searchStands(name, locationIds, type, dateStart, pageable)
                .map(standMapper::toFestivalStandResponse);
    }

    @Transactional(readOnly = true)
    public List<FestivalStandResponse> getStandsByFestival(Long festivalLocationId) {
        return festivalStandRepository.findAllByFestivalLocationId(festivalLocationId)
                .stream()
                .map(standMapper::toFestivalStandResponse)
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

        Long organizerCompanyId = request.getOrganizerCompanyId() != null ? request.getOrganizerCompanyId() : companyId;

        StandInstance instance = standInstanceRepository.findIdenticalInstance(
                request.getStandId(),
                organizerCompanyId,
                request.getType(),
                request.getDateStart(),
                request.getDescription()
        ).orElseGet(() -> {
            StandInstance newInstance = StandInstance.builder()
                    .stand(stand)
                    .companyId(organizerCompanyId)
                    .standType(request.getType())
                    .description(request.getDescription())
                    .dateStart(request.getDateStart())
                    .build();

            return standInstanceRepository.save(newInstance);
        });

        FestivalStand festivalStand = FestivalStand.builder()
                .festivalLocationId(request.getFestivalLocationId())
                .standInstance(instance)
                .build();

        return festivalStandRepository.save(festivalStand).getId();
    }
}
