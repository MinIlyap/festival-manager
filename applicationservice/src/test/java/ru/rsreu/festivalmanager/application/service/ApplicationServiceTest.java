package ru.rsreu.festivalmanager.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.rsreu.festivalmanager.application.dto.ReviewRequestDto;
import ru.rsreu.festivalmanager.application.entity.Application;
import ru.rsreu.festivalmanager.application.entity.ApplicationData;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.entity.ApplicationType;
import ru.rsreu.festivalmanager.application.event.ApplicationEvents;
import ru.rsreu.festivalmanager.application.repository.ApplicationRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    private static final OffsetDateTime EVENT_TIME = OffsetDateTime.parse("2026-06-01T12:00:00+03:00");

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ApplicationService applicationService;

    private static Application existing(long id, ApplicationType type, ApplicationStatus status) {
        ApplicationData data = new ApplicationData();
        data.setCompanyId(5L);
        data.setFestivalLocationId(11L);

        Application application = new Application();
        application.setId(id);
        application.setApplicationType(type);
        application.setApplicationStatus(status);
        application.setSenderId(42L);
        application.setAgentId(9L);
        application.setCreatedAt(EVENT_TIME.minusDays(5));
        application.setUpdatedAt(EVENT_TIME.minusDays(5));
        application.setData(data);
        return application;
    }

    private Application captureSaved() {
        ArgumentCaptor<Application> saved = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(saved.capture());
        return saved.getValue();
    }

    @Nested
    @DisplayName("Поступление заявки компании")
    class IncomingCompanyApplication {

        @Test
        @DisplayName("первая заявка создаётся в статусе PENDING")
        void createsPendingApplication() {
            when(applicationRepository.findByData_CompanyId(5L)).thenReturn(Optional.empty());

            applicationService.processCompanyApplication(5L, EVENT_TIME);

            Application saved = captureSaved();
            assertThat(saved.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(saved.getApplicationType()).isEqualTo(ApplicationType.COMPANY_APPLICATION);
            assertThat(saved.getData().getCompanyId()).isEqualTo(5L);
            assertThat(saved.getCreatedAt()).isEqualTo(EVENT_TIME);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("уже одобренную заявку повторная подача не трогает")
        void ignoresApprovedApplication() {
            when(applicationRepository.findByData_CompanyId(5L))
                    .thenReturn(Optional.of(existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.APPROVED)));

            applicationService.processCompanyApplication(5L, EVENT_TIME);

            // иначе одобренную компанию можно было бы отправить на повторную модерацию
            verify(applicationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("отклонённая заявка возвращается в PENDING и уведомляет администратора")
        void resubmitsRejectedApplication() {
            Application rejected = existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.REJECTED);
            when(applicationRepository.findByData_CompanyId(5L)).thenReturn(Optional.of(rejected));

            applicationService.processCompanyApplication(5L, EVENT_TIME);

            assertThat(rejected.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(rejected.getUpdatedAt()).isEqualTo(EVENT_TIME);
            verify(applicationRepository).save(rejected);

            ArgumentCaptor<ApplicationEvents.AppResubmittedEvent> event =
                    ArgumentCaptor.forClass(ApplicationEvents.AppResubmittedEvent.class);
            verify(eventPublisher).publishEvent(event.capture());
            assertThat(event.getValue().applicationId()).isEqualTo(1L);
            assertThat(event.getValue().agentId()).isEqualTo(9L);
            assertThat(event.getValue().type()).isEqualTo("COMPANY_APPLICATION");
            // для заявки компании в событие уезжает companyId
            assertThat(event.getValue().entityId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("заявка на рассмотрении только обновляет дату, без повторного уведомления")
        void refreshesPendingApplication() {
            Application pending = existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.PENDING);
            when(applicationRepository.findByData_CompanyId(5L)).thenReturn(Optional.of(pending));

            applicationService.processCompanyApplication(5L, EVENT_TIME);

            assertThat(pending.getUpdatedAt()).isEqualTo(EVENT_TIME);
            verify(applicationRepository).save(pending);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("без времени события подставляется текущее")
        void fallsBackToNowWhenTimestampIsMissing() {
            when(applicationRepository.findByData_CompanyId(5L)).thenReturn(Optional.empty());

            applicationService.processCompanyApplication(5L, null);

            assertThat(captureSaved().getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Поступление заявки на фестиваль")
    class IncomingFestivalApplication {

        @Test
        @DisplayName("заявка создаётся с автором и площадкой фестиваля")
        void createsApplicationWithSender() {
            when(applicationRepository.findByData_FestivalLocationId(11L)).thenReturn(Optional.empty());

            applicationService.processFestivalApplication(11L, 42L, EVENT_TIME);

            Application saved = captureSaved();
            assertThat(saved.getApplicationType()).isEqualTo(ApplicationType.FESTIVAL_APPLICATION);
            assertThat(saved.getSenderId()).isEqualTo(42L);
            assertThat(saved.getData().getFestivalLocationId()).isEqualTo(11L);
            assertThat(saved.getData().getCompanyId()).isNull();
        }

        @Test
        @DisplayName("при переподаче в событие уезжает id площадки, а не компании")
        void resubmitCarriesFestivalLocationId() {
            Application rejected = existing(2L, ApplicationType.FESTIVAL_APPLICATION, ApplicationStatus.REJECTED);
            when(applicationRepository.findByData_FestivalLocationId(11L)).thenReturn(Optional.of(rejected));

            applicationService.processFestivalApplication(11L, 42L, EVENT_TIME);

            ArgumentCaptor<ApplicationEvents.AppResubmittedEvent> event =
                    ArgumentCaptor.forClass(ApplicationEvents.AppResubmittedEvent.class);
            verify(eventPublisher).publishEvent(event.capture());
            assertThat(event.getValue().entityId()).isEqualTo(11L);
            assertThat(event.getValue().type()).isEqualTo("FESTIVAL_APPLICATION");
        }
    }

    @Nested
    @DisplayName("Рассмотрение заявки администратором")
    class ReviewApplication {

        private ReviewRequestDto review(ApplicationStatus status, String comment) {
            ReviewRequestDto dto = new ReviewRequestDto();
            dto.setStatus(status);
            dto.setComment(comment);
            return dto;
        }

        @Test
        @DisplayName("одобрение фиксирует администратора и порождает событие ответа")
        void approvesAndPublishesResponse() {
            Application pending = existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.PENDING);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(pending));

            applicationService.reviewApplication(1L, review(ApplicationStatus.APPROVED, "ок"), 77L);

            assertThat(pending.getApplicationStatus()).isEqualTo(ApplicationStatus.APPROVED);
            assertThat(pending.getAgentId()).isEqualTo(77L);
            assertThat(pending.getData().getComment()).isEqualTo("ок");
            verify(applicationRepository).save(pending);

            ArgumentCaptor<ApplicationEvents.AppResponseEvent> event =
                    ArgumentCaptor.forClass(ApplicationEvents.AppResponseEvent.class);
            verify(eventPublisher).publishEvent(event.capture());
            assertThat(event.getValue().status()).isEqualTo(ApplicationStatus.APPROVED);
            assertThat(event.getValue().companyId()).isEqualTo(5L);
            assertThat(event.getValue().senderId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("повторное рассмотрение одобренной заявки запрещено")
        void rejectsReviewOfApprovedApplication() {
            when(applicationRepository.findById(1L))
                    .thenReturn(Optional.of(existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.APPROVED)));

            assertThatThrownBy(() -> applicationService.reviewApplication(1L, review(ApplicationStatus.REJECTED, "причина"), 77L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("уже одобрена");

            verify(applicationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("отклонение без комментария не проходит")
        void requiresCommentOnRejection() {
            when(applicationRepository.findById(1L))
                    .thenReturn(Optional.of(existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.PENDING)));

            assertThatThrownBy(() -> applicationService.reviewApplication(1L, review(ApplicationStatus.REJECTED, null), 77L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("комментарий");

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("комментарий из пробелов тоже не считается комментарием")
        void requiresNonBlankComment() {
            when(applicationRepository.findById(1L))
                    .thenReturn(Optional.of(existing(1L, ApplicationType.COMPANY_APPLICATION, ApplicationStatus.PENDING)));

            assertThatThrownBy(() -> applicationService.reviewApplication(1L, review(ApplicationStatus.REJECTED, "   "), 77L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("комментарий");
        }

        @Test
        @DisplayName("несуществующая заявка — ошибка")
        void failsOnMissingApplication() {
            when(applicationRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.reviewApplication(404L, review(ApplicationStatus.APPROVED, null), 77L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("не найдена");
        }
    }
}
