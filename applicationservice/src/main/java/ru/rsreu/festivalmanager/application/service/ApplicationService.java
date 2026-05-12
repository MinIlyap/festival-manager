package ru.rsreu.festivalmanager.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.application.dto.ReviewRequestDto;
import ru.rsreu.festivalmanager.application.entity.*;
import ru.rsreu.festivalmanager.application.repository.ApplicationRepository;
import ru.rsreu.festivalmanager.application.event.ApplicationEvents.AppResponseEvent;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processCompanyApplication(Long companyId, OffsetDateTime createdAt) {
        Application application = applicationRepository.findByCompanyId(companyId)
                .orElseGet(() -> createNewApplication(ApplicationType.COMPANY_APPLICATION, null, companyId, null, createdAt));

        handleIncomingApplication(application, createdAt);
    }

    @Transactional
    public void processFestivalApplication(Long festivalLocationId, Long senderId, OffsetDateTime createdAt) {
        Application application = applicationRepository.findByFestivalLocationId(festivalLocationId)
                .orElseGet(() -> createNewApplication(ApplicationType.FESTIVAL_APPLICATION, senderId, null, festivalLocationId, createdAt));

        handleIncomingApplication(application, createdAt);
    }

    private Application createNewApplication(ApplicationType type, Long senderId, Long companyId, Long festivalLocationId, OffsetDateTime createdAt) {
        Application app = new Application();
        app.setApplicationType(type);
        app.setSenderId(senderId);

        OffsetDateTime now = createdAt != null ? createdAt : OffsetDateTime.now();
        app.setCreatedAt(now);
        app.setUpdatedAt(now);

        ApplicationData data = new ApplicationData();
        data.setCompanyId(companyId);
        data.setFestivalLocationId(festivalLocationId);
        app.setData(data);

        return app;
    }

    private void handleIncomingApplication(Application application, OffsetDateTime timestamp) {
        if (application.getApplicationStatus() == ApplicationStatus.APPROVED) {
            log.warn("Попытка переподачи одобренной заявки ID {}", application.getId());
            return;
        }

        if (application.getApplicationStatus() == ApplicationStatus.REJECTED) {
            application.setApplicationStatus(ApplicationStatus.PENDING);
            application.setUpdatedAt(timestamp != null ? timestamp : OffsetDateTime.now());
            applicationRepository.save(application);
            log.info("Отклоненная заявка ID {} переведена в PENDING. Старый комментарий сохранен для контекста админа.", application.getId());
            return;
        }

        if (application.getApplicationStatus() == ApplicationStatus.PENDING) {
            application.setUpdatedAt(timestamp != null ? timestamp : OffsetDateTime.now());
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

        if (dto.getStatus() == ApplicationStatus.APPROVED) {
            application.getData().setComment(null);
        } else if (dto.getStatus() == ApplicationStatus.REJECTED) {
            application.getData().setComment(dto.getComment());
        }

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

    public List<Application> getMyApplications(Long companyId, ApplicationStatus status) {
        return applicationRepository.findAllBySenderIdWithFilters(companyId, status);
    }

    public List<Application> getAllApplicationsForAdmin(ApplicationType type, ApplicationStatus status) {
        return applicationRepository.findAllWithFilters(type, status);
    }

    public List<Application> getAdminPersonalHistory(Long agentId, ApplicationType type, ApplicationStatus status) {
        return applicationRepository.findAdminHistory(agentId, type, status);
    }
}