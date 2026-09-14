package ru.rsreu.festivalmanager.company.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.rsreu.festivalmanager.company.dto.CompanyCreateRequest;
import ru.rsreu.festivalmanager.company.dto.CompanyResponse;
import ru.rsreu.festivalmanager.company.dto.CompanyUpdateRequest;
import ru.rsreu.festivalmanager.company.entity.Company;
import ru.rsreu.festivalmanager.company.event.CompanyEvents;
import ru.rsreu.festivalmanager.company.mapper.CompanyMapper;
import ru.rsreu.festivalmanager.company.mapper.CompanyMapperImpl;
import ru.rsreu.festivalmanager.company.repository.CompanyRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    // настоящий маппер: тесты заодно проверяют преобразование в dto
    private final CompanyMapper companyMapper = new CompanyMapperImpl();

    private CompanyService service() {
        return new CompanyService(companyRepository, companyMapper, eventPublisher);
    }

    private static Company company(long id, String tin, String email, boolean active) {
        Company company = new Company();
        company.setId(id);
        company.setTin(tin);
        company.setEmail(email);
        company.setFullName("ООО Ромашка");
        company.setBrandName("Ромашка");
        company.setWebsite("https://romashka.example");
        company.setActive(active);
        return company;
    }

    private static CompanyCreateRequest createRequest() {
        CompanyCreateRequest request = new CompanyCreateRequest();
        request.setTin("1234567890");
        request.setFullName("ООО Ромашка");
        request.setBrandName("Ромашка");
        request.setEmail("info@romashka.example");
        request.setWebsite("https://romashka.example");
        return request;
    }

    /** Makes save() behave like JPA: return what it was given. */
    private void echoSave() {
        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("Подача заявки компании")
    class CreateInactiveCompany {

        @Test
        @DisplayName("новая компания создаётся неактивной и порождает событие заявки")
        void createsInactiveCompanyAndPublishesEvent() {
            when(companyRepository.findByTin("1234567890")).thenReturn(Optional.empty());
            when(companyRepository.findByEmail("info@romashka.example")).thenReturn(Optional.empty());
            when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
                Company saved = invocation.getArgument(0);
                saved.setId(5L);
                return saved;
            });

            Long id = service().createInactiveCompany(createRequest());

            ArgumentCaptor<Company> saved = ArgumentCaptor.forClass(Company.class);
            verify(companyRepository).save(saved.capture());
            assertThat(saved.getValue().isActive()).isFalse();
            assertThat(saved.getValue().getTin()).isEqualTo("1234567890");
            assertThat(id).isEqualTo(5L);

            ArgumentCaptor<CompanyEvents.CompanyAppRequestEvent> event =
                    ArgumentCaptor.forClass(CompanyEvents.CompanyAppRequestEvent.class);
            verify(eventPublisher).publishEvent(event.capture());
            assertThat(event.getValue().companyId()).isEqualTo(5L);
            assertThat(event.getValue().companyName()).isEqualTo("ООО Ромашка");
        }

        @Test
        @DisplayName("повторная заявка по тому же ИНН обновляет неактивную запись, а не создаёт вторую")
        void reusesInactiveCompanyFoundByTin() {
            Company existing = company(5L, "1234567890", "old@romashka.example", false);
            existing.setBrandName("Старый бренд");
            when(companyRepository.findByTin("1234567890")).thenReturn(Optional.of(existing));
            when(companyRepository.findByEmail("info@romashka.example")).thenReturn(Optional.empty());
            echoSave();

            Long id = service().createInactiveCompany(createRequest());

            assertThat(id).isEqualTo(5L);
            assertThat(existing.getBrandName()).isEqualTo("Ромашка");
            assertThat(existing.getEmail()).isEqualTo("info@romashka.example");
            verify(companyRepository).save(existing);
        }

        @Test
        @DisplayName("неактивная запись находится и по email, если ИНН изменился")
        void reusesInactiveCompanyFoundByEmail() {
            Company existing = company(5L, "0000000000", "info@romashka.example", false);
            when(companyRepository.findByTin("1234567890")).thenReturn(Optional.empty());
            when(companyRepository.findByEmail("info@romashka.example")).thenReturn(Optional.of(existing));
            echoSave();

            Long id = service().createInactiveCompany(createRequest());

            assertThat(id).isEqualTo(5L);
            assertThat(existing.getTin()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("уже активная компания не может подать заявку повторно")
        void rejectsAlreadyActiveCompany() {
            Company active = company(5L, "1234567890", "info@romashka.example", true);
            when(companyRepository.findByTin("1234567890")).thenReturn(Optional.of(active));
            when(companyRepository.findByEmail("info@romashka.example")).thenReturn(Optional.of(active));

            assertThatThrownBy(() -> service().createInactiveCompany(createRequest()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("активна");

            verify(companyRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("Активация и чтение")
    class ActivationAndLookup {

        @Test
        @DisplayName("активация выставляет флаг и сохраняет")
        void activatesCompany() {
            Company inactive = company(5L, "1234567890", "info@romashka.example", false);
            when(companyRepository.findById(5L)).thenReturn(Optional.of(inactive));

            service().activateCompany(5L);

            assertThat(inactive.isActive()).isTrue();
            verify(companyRepository).save(inactive);
        }

        @Test
        @DisplayName("поиск по неизвестному ИНН сообщает этот ИНН")
        void failsWithTinInMessage() {
            when(companyRepository.findByTin("9999999999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service().getCompanyByTin("9999999999"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("9999999999");
        }

        @Test
        @DisplayName("флаг активности доезжает до dto")
        void mapsActiveFlag() {
            when(companyRepository.findById(5L))
                    .thenReturn(Optional.of(company(5L, "1234567890", "info@romashka.example", true)));

            CompanyResponse response = service().getCompanyById(5L);

            assertThat(response.isActive()).isTrue();
            assertThat(response.getTin()).isEqualTo("1234567890");
            assertThat(response.getEmail()).isEqualTo("info@romashka.example");
        }
    }

    @Nested
    @DisplayName("Обновление данных")
    class Updates {

        @Test
        @DisplayName("обновление профиля не меняет ИНН и email")
        void doesNotTouchTinAndEmail() {
            Company existing = company(5L, "1234567890", "info@romashka.example", true);
            when(companyRepository.findById(5L)).thenReturn(Optional.of(existing));
            echoSave();

            CompanyUpdateRequest request = new CompanyUpdateRequest();
            request.setFullName("ООО Новая Ромашка");
            request.setBrandName("Новый бренд");
            request.setWebsite("https://new.example");

            CompanyResponse response = service().updateCompany(5L, request);

            assertThat(response.getFullName()).isEqualTo("ООО Новая Ромашка");
            assertThat(response.getBrandName()).isEqualTo("Новый бренд");
            // ИНН и email меняются отдельными сценариями, здесь должны остаться прежними
            assertThat(response.getTin()).isEqualTo("1234567890");
            assertThat(response.getEmail()).isEqualTo("info@romashka.example");
        }

        @Test
        @DisplayName("смена email у несуществующей компании — ошибка")
        void failsToUpdateEmailOfMissingCompany() {
            when(companyRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service().updateEmail(404L, "new@example.com"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(companyRepository, never()).save(any());
        }
    }
}
