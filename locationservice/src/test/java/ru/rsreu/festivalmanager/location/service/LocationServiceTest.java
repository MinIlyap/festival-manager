package ru.rsreu.festivalmanager.location.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.rsreu.festivalmanager.location.dto.CityCreateRequest;
import ru.rsreu.festivalmanager.location.dto.CityResponse;
import ru.rsreu.festivalmanager.location.dto.PlatformInstanceCreateRequest;
import ru.rsreu.festivalmanager.location.dto.PlatformInstanceResponse;
import ru.rsreu.festivalmanager.location.dto.PlatformInstanceUpdateRequest;
import ru.rsreu.festivalmanager.location.entity.City;
import ru.rsreu.festivalmanager.location.entity.Platform;
import ru.rsreu.festivalmanager.location.entity.PlatformInstance;
import ru.rsreu.festivalmanager.location.mapper.LocationMapper;
import ru.rsreu.festivalmanager.location.mapper.LocationMapperImpl;
import ru.rsreu.festivalmanager.location.repository.CityRepository;
import ru.rsreu.festivalmanager.location.repository.PlatformInstanceRepository;
import ru.rsreu.festivalmanager.location.repository.PlatformRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock private CityRepository cityRepository;
    @Mock private PlatformRepository platformRepository;
    @Mock private PlatformInstanceRepository instanceRepository;

    private final LocationMapper locationMapper = new LocationMapperImpl();

    private LocationService service() {
        return new LocationService(cityRepository, platformRepository, instanceRepository, locationMapper);
    }

    private static City city(long id, String name) {
        City city = new City();
        city.setId(id);
        city.setName(name);
        return city;
    }

    private static Platform platform(long id, String name) {
        Platform platform = new Platform();
        platform.setId(id);
        platform.setName(name);
        return platform;
    }

    private static PlatformInstance instance(long id, Platform platform, City city, String street, String house) {
        PlatformInstance instance = new PlatformInstance();
        instance.setId(id);
        instance.setPlatform(platform);
        instance.setCity(city);
        instance.setStreet(street);
        instance.setHouse(house);
        return instance;
    }

    @Nested
    @DisplayName("Города")
    class Cities {

        private CityCreateRequest request(String name) {
            CityCreateRequest request = new CityCreateRequest();
            request.setName(name);
            return request;
        }

        @Test
        @DisplayName("город с таким же названием в другом регистре не создаётся")
        void rejectsDuplicateIgnoringCase() {
            when(cityRepository.existsByNameIgnoreCase("рязань")).thenReturn(true);

            assertThatThrownBy(() -> service().addCity(request("рязань")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("уже существует");

            verify(cityRepository, never()).save(any());
        }

        @Test
        @DisplayName("новый город сохраняется и возвращается с присвоенным id")
        void savesNewCity() {
            when(cityRepository.existsByNameIgnoreCase("Рязань")).thenReturn(false);
            when(cityRepository.save(any(City.class))).thenReturn(city(7L, "Рязань"));

            CityResponse response = service().addCity(request("Рязань"));

            assertThat(response.getId()).isEqualTo(7L);
            assertThat(response.getName()).isEqualTo("Рязань");
        }

        @Test
        @DisplayName("несуществующий город — ошибка с указанием id")
        void failsOnMissingCity() {
            when(cityRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service().getCityById(404L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("404");
        }

        @Test
        @DisplayName("поиск городов отдаёт страницу уже в виде dto")
        void mapsSearchResults() {
            Pageable pageable = PageRequest.of(0, 2);
            when(cityRepository.findByNameContainingIgnoreCase("Ряз", pageable))
                    .thenReturn(new PageImpl<>(List.of(city(7L, "Рязань")), pageable, 1));

            Page<CityResponse> page = service().searchCities("Ряз", pageable);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().getFirst().getName()).isEqualTo("Рязань");
        }
    }

    @Nested
    @DisplayName("Площадки")
    class Platforms {

        private PlatformInstanceCreateRequest request() {
            PlatformInstanceCreateRequest request = new PlatformInstanceCreateRequest();
            request.setPlatformName("Экспоцентр");
            request.setCityId(7L);
            request.setStreet("Ленина");
            request.setHouse("1");
            return request;
        }

        @Test
        @DisplayName("тот же адрес повторно не создаётся, возвращается существующая площадка")
        void reusesExistingInstance() {
            City city = city(7L, "Рязань");
            Platform platform = platform(3L, "Экспоцентр");
            when(cityRepository.findById(7L)).thenReturn(Optional.of(city));
            when(platformRepository.findByNameIgnoreCase("Экспоцентр")).thenReturn(Optional.of(platform));
            when(instanceRepository.findByPlatform_IdAndCity_IdAndStreetAndHouse(3L, 7L, "Ленина", "1"))
                    .thenReturn(Optional.of(instance(11L, platform, city, "Ленина", "1")));

            PlatformInstanceResponse response = service().createOrGetPlatformInstance(request());

            assertThat(response.getId()).isEqualTo(11L);
            assertThat(response.getPlatformName()).isEqualTo("Экспоцентр");
            assertThat(response.getCity().getName()).isEqualTo("Рязань");
            verify(instanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("неизвестное название площадки создаётся один раз и переиспользуется для адреса")
        void createsPlatformWhenNameIsNew() {
            City city = city(7L, "Рязань");
            Platform created = platform(3L, "Экспоцентр");
            when(cityRepository.findById(7L)).thenReturn(Optional.of(city));
            when(platformRepository.findByNameIgnoreCase("Экспоцентр")).thenReturn(Optional.empty());
            when(platformRepository.save(any(Platform.class))).thenReturn(created);
            when(instanceRepository.findByPlatform_IdAndCity_IdAndStreetAndHouse(3L, 7L, "Ленина", "1"))
                    .thenReturn(Optional.empty());
            when(instanceRepository.save(any(PlatformInstance.class)))
                    .thenAnswer(invocation -> {
                        PlatformInstance saved = invocation.getArgument(0);
                        saved.setId(11L);
                        return saved;
                    });

            PlatformInstanceResponse response = service().createOrGetPlatformInstance(request());

            ArgumentCaptor<PlatformInstance> saved = ArgumentCaptor.forClass(PlatformInstance.class);
            verify(instanceRepository).save(saved.capture());
            assertThat(saved.getValue().getPlatform()).isSameAs(created);
            assertThat(saved.getValue().getCity()).isSameAs(city);
            assertThat(saved.getValue().getStreet()).isEqualTo("Ленина");
            assertThat(saved.getValue().getHouse()).isEqualTo("1");
            assertThat(response.getId()).isEqualTo(11L);
        }

        @Test
        @DisplayName("площадка в несуществующем городе не создаётся")
        void failsWhenCityIsMissing() {
            when(cityRepository.findById(7L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service().createOrGetPlatformInstance(request()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Город не найден");

            verify(instanceRepository, never()).save(any());
            verify(platformRepository, never()).save(any());
        }

        @Test
        @DisplayName("без города ищем по названию, с городом — по городу и названию")
        void picksQueryDependingOnCityFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PlatformInstance> empty = new PageImpl<>(List.of(), pageable, 0);
            when(instanceRepository.findByPlatform_NameContainingIgnoreCase("Экспо", pageable)).thenReturn(empty);
            when(instanceRepository.findByCity_IdAndPlatform_NameContainingIgnoreCase(7L, "Экспо", pageable))
                    .thenReturn(empty);

            service().searchPlatformInstances(null, "Экспо", pageable);
            service().searchPlatformInstances(7L, "Экспо", pageable);

            verify(instanceRepository).findByPlatform_NameContainingIgnoreCase("Экспо", pageable);
            verify(instanceRepository).findByCity_IdAndPlatform_NameContainingIgnoreCase(7L, "Экспо", pageable);
            verify(instanceRepository, never())
                    .findByCity_IdAndPlatform_NameContainingIgnoreCase(eq(null), any(), any());
        }

        @Test
        @DisplayName("обновление меняет адрес, но не город и не название площадки")
        void updateChangesAddressOnly() {
            City city = city(7L, "Рязань");
            Platform platform = platform(3L, "Экспоцентр");
            PlatformInstance existing = instance(11L, platform, city, "Ленина", "1");
            when(instanceRepository.findById(11L)).thenReturn(Optional.of(existing));
            when(instanceRepository.save(existing)).thenReturn(existing);

            PlatformInstanceUpdateRequest request = new PlatformInstanceUpdateRequest();
            request.setStreet("Победы");
            request.setHouse("25А");

            PlatformInstanceResponse response = service().updatePlatformInstance(11L, request);

            assertThat(response.getStreet()).isEqualTo("Победы");
            assertThat(response.getHouse()).isEqualTo("25А");
            assertThat(response.getCity().getName()).isEqualTo("Рязань");
            assertThat(response.getPlatformName()).isEqualTo("Экспоцентр");
        }
    }
}
