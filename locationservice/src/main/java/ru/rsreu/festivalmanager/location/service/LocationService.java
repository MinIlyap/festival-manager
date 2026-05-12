package ru.rsreu.festivalmanager.location.service;

import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

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

    public List<CityResponse> searchCities(String namePart) {
        return cityRepository.findByNameContainingIgnoreCase(namePart)
                .stream()
                .map(this::mapToCityResponse)
                .collect(Collectors.toList());
    }

    public boolean checkCityExists(Long id) {
        return cityRepository.existsById(id);
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

        return instanceRepository.findExistingInstance(platform.getId(), city.getId(), request.getStreet(), request.getHouse())
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

    public List<PlatformInstanceResponse> searchPlatformInstances(Long cityId, String platformNamePart) {
        return instanceRepository.searchInstancesByCityAndPlatformName(cityId, platformNamePart)
                .stream()
                .map(this::mapToInstanceResponse)
                .collect(Collectors.toList());
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
