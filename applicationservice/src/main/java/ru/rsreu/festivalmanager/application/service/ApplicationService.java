package ru.rsreu.festivalmanager.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.application.dto.ReviewRequestDto;
import ru.rsreu.festivalmanager.application.entity.*;
import ru.rsreu.festivalmanager.application.event.ApplicationEvents;
import ru.rsreu.festivalmanager.application.event.ApplicationEvents.AppResponseEvent;
import ru.rsreu.festivalmanager.application.repository.ApplicationRepository;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processCompanyApplication(Long companyId, OffsetDateTime eventTimestamp) {
        Application application = applicationRepository.findByCompanyId(companyId)
                .orElseGet(() -> createNewApplication(ApplicationType.COMPANY_APPLICATION, null, companyId, null, eventTimestamp));

        handleIncomingApplication(application, eventTimestamp);
    }

    @Transactional
    public void processFestivalApplication(Long festivalLocationId, Long senderId, OffsetDateTime eventTimestamp) {
        Application application = applicationRepository.findByFestivalLocationId(festivalLocationId)
                .orElseGet(() -> createNewApplication(ApplicationType.FESTIVAL_APPLICATION, senderId, null, festivalLocationId, eventTimestamp));

        handleIncomingApplication(application, eventTimestamp);
    }

    private Application createNewApplication(ApplicationType type, Long senderId, Long companyId, Long festivalLocationId, OffsetDateTime eventTimestamp) {
        Application app = new Application();
        app.setApplicationType(type);
        app.setApplicationStatus(ApplicationStatus.PENDING);
        app.setSenderId(senderId);

        OffsetDateTime now = eventTimestamp != null ? eventTimestamp : OffsetDateTime.now();
        app.setCreatedAt(now);
        app.setUpdatedAt(now);

        ApplicationData data = new ApplicationData();
        data.setCompanyId(companyId);
        data.setFestivalLocationId(festivalLocationId);
        app.setData(data);

        return app;
    }

    private void handleIncomingApplication(Application application, OffsetDateTime eventTimestamp) {
        if (application.getId() == null) {
            applicationRepository.save(application);
            log.info("Создана новая заявка типа {}", application.getApplicationType());
            return;
        }

        if (application.getApplicationStatus() == ApplicationStatus.APPROVED) {
            log.warn("Попытка переподачи одобренной заявки ID {}", application.getId());
            return;
        }

        if (application.getApplicationStatus() == ApplicationStatus.REJECTED) {
            application.setApplicationStatus(ApplicationStatus.PENDING);
            application.setUpdatedAt(eventTimestamp != null ? eventTimestamp : OffsetDateTime.now());

            applicationRepository.save(application);
            log.info("Отклоненная заявка ID {} переведена в PENDING. Старый комментарий сохранен для контекста админа.", application.getId());

            Long entityId = application.getApplicationType() == ApplicationType.COMPANY_APPLICATION ?
                    application.getData().getCompanyId() : application.getData().getFestivalLocationId();

            eventPublisher.publishEvent(new ApplicationEvents.AppResubmittedEvent(
                    application.getAgentId(),
                    application.getApplicationType().name(),
                    entityId,
                    application.getUpdatedAt()
            ));
            return;
        }

        if (application.getApplicationStatus() == ApplicationStatus.PENDING) {
            application.setUpdatedAt(eventTimestamp != null ? eventTimestamp : OffsetDateTime.now());
            applicationRepository.save(application);
        }
    }

    @Transactional
    public void reviewApplication(Long applicationId, ReviewRequestDto dto, Long adminId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

        if (application.getApplicationStatus() == ApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("Заявка уже одобрена");
        }

        if (dto.getStatus() == ApplicationStatus.REJECTED && (dto.getComment() == null || dto.getComment().isBlank())) {
            throw new IllegalArgumentException("При отклонении заявки необходим комментарий");
        }

        application.setApplicationStatus(dto.getStatus());
        application.getData().setComment(dto.getComment());
        application.setAgentId(adminId);
        application.setUpdatedAt(OffsetDateTime.now());

        applicationRepository.save(application);

        eventPublisher.publishEvent(new AppResponseEvent(
                application.getApplicationType(),
                application.getApplicationStatus(),
                application.getData().getComment(),
                application.getData().getCompanyId(),
                application.getData().getFestivalLocationId(),
                application.getUpdatedAt()
        ));
    }

    public Page<Application> getMyApplications(Long companyId, ApplicationStatus status, Pageable pageable) {
        return applicationRepository.findAllBySenderIdWithFilters(companyId, status, pageable);
    }

    public Page<Application> getAllApplicationsForAdmin(ApplicationType type, ApplicationStatus status, Pageable pageable) {
        return applicationRepository.findAllWithFilters(type, status, pageable);
    }

    public Page<Application> getAdminPersonalHistory(Long agentId, ApplicationType type, ApplicationStatus status, Pageable pageable) {
        return applicationRepository.findAdminHistory(agentId, type, status, pageable);
    }
}