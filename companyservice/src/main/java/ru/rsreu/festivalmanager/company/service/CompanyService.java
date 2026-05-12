package ru.rsreu.festivalmanager.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.company.dto.CompanyCreateRequest;
import ru.rsreu.festivalmanager.company.dto.CompanyResponse;
import ru.rsreu.festivalmanager.company.dto.CompanyUpdateRequest;
import ru.rsreu.festivalmanager.company.entity.Company;
import ru.rsreu.festivalmanager.company.event.CompanyEvents;
import ru.rsreu.festivalmanager.company.repository.CompanyRepository;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createInactiveCompany(CompanyCreateRequest request) {
        if (companyRepository.existsByTin(request.getTin())) {
            throw new IllegalArgumentException("Компания с таким ИНН уже существует");
        }
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Компания с таким Email уже существует");
        }

        Company company = new Company();
        company.setTin(request.getTin());
        company.setFullName(request.getFullName());
        company.setBrandName(request.getBrandName());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setActive(false);

        Long companyId = companyRepository.save(company).getId();

        eventPublisher.publishEvent(new CompanyEvents.CompanyAppRequestEvent(companyId,
                company.getFullName(), OffsetDateTime.now()));

        return companyId;
    }

    @Transactional
    public void activateCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));
        company.setActive(true);
        companyRepository.save(company);
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));

        company.setFullName(request.getFullName());
        company.setBrandName(request.getBrandName());
        company.setWebsite(request.getWebsite());

        return mapToResponse(companyRepository.save(company));
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .tin(company.getTin())
                .fullName(company.getFullName())
                .brandName(company.getBrandName())
                .email(company.getEmail())
                .website(company.getWebsite())
                .isActive(company.isActive())
                .build();
    }
}
