package ru.rsreu.festivalmanager.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.location.dto.*;
import ru.rsreu.festivalmanager.location.entity.City;
import ru.rsreu.festivalmanager.location.entity.Platform;
import ru.rsreu.festivalmanager.location.entity.PlatformInstance;
import ru.rsreu.festivalmanager.location.repository.CityRepository;
import ru.rsreu.festivalmanager.location.repository.PlatformInstanceRepository;
import ru.rsreu.festivalmanager.location.repository.PlatformRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final CityRepository cityRepository;
    private final PlatformRepository platformRepository;
    private final PlatformInstanceRepository instanceRepository;

    @Transactional
    public CityResponse addCity(CityCreateRequest request) {
        if (cityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Такой город уже существует");
        }

        City city = new City();
        city.setName(request.getName());
        city = cityRepository.save(city);

        return mapToCityResponse(city);
    }

    @Transactional(readOnly = true)
    public Page<CityResponse> searchCities(String namePart, Pageable pageable) {
        return cityRepository.findByNameContainingIgnoreCase(namePart, pageable)
                .map(this::mapToCityResponse);
    }

    @Transactional(readOnly = true)
    public boolean checkCityExists(Long id) {
        return cityRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Город не найден: " + id));

        return mapToCityResponse(city);
    }

    @Transactional
    public PlatformInstanceResponse createOrGetPlatformInstance(PlatformInstanceCreateRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new IllegalArgumentException("Город не найден"));

        Platform platform = platformRepository.findByNameIgnoreCase(request.getPlatformName())
                .orElseGet(() -> {
                    Platform newPlatform = new Platform();
                    newPlatform.setName(request.getPlatformName());

                    return platformRepository.save(newPlatform);
                });

        return instanceRepository.findByPlatform_IdAndCity_IdAndStreetAndHouse(platform.getId(), city.getId(), request.getStreet(), request.getHouse())
                .map(this::mapToInstanceResponse)
                .orElseGet(() -> {
                    PlatformInstance newInstance = new PlatformInstance();
                    newInstance.setPlatform(platform);
                    newInstance.setCity(city);
                    newInstance.setStreet(request.getStreet());
                    newInstance.setHouse(request.getHouse());

                    return mapToInstanceResponse(instanceRepository.save(newInstance));
                });
    }

    @Transactional(readOnly = true)
    public Page<PlatformInstanceResponse> searchPlatformInstances(Long cityId, String platformNamePart, Pageable pageable) {
        if (cityId == null) {
            return instanceRepository.findByPlatform_NameContainingIgnoreCase(platformNamePart, pageable)
                    .map(this::mapToInstanceResponse);
        }
        return instanceRepository.findByCity_IdAndPlatform_NameContainingIgnoreCase(cityId, platformNamePart, pageable)
                .map(this::mapToInstanceResponse);
    }

    @Transactional
    public PlatformInstanceResponse updatePlatformInstance(Long id, PlatformInstanceUpdateRequest request) {
        PlatformInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Площадка не найдена"));

        instance.setStreet(request.getStreet());
        instance.setHouse(request.getHouse());

        return mapToInstanceResponse(instanceRepository.save(instance));
    }

    @Transactional(readOnly = true)
    public boolean isPlatformInstanceExistsById(Long id) {
        return instanceRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public PlatformInstanceResponse getPlatformInstanceById(Long id) {
        PlatformInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Площадка не найдена: " + id));
        return mapToInstanceResponse(instance);
    }

    @Transactional(readOnly = true)
    public List<Long> getPlatformIdsByCity(Long cityId) {
        return instanceRepository.findPlatformInstanceIdsByCityId(cityId);
    }

    private CityResponse mapToCityResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    private PlatformInstanceResponse mapToInstanceResponse(PlatformInstance instance) {
        return PlatformInstanceResponse.builder()
                .id(instance.getId())
                .platformName(instance.getPlatform().getName())
                .city(mapToCityResponse(instance.getCity()))
                .street(instance.getStreet())
                .house(instance.getHouse())
                .build();
    }
}
